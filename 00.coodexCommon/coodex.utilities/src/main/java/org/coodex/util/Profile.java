/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.coodex.util.Common.*;

/**
 * 自财运通项目中移植过来utilities中<br>
 * 2016/09/05 废弃<S>
 * 2014/04/27 支持命名空间<br>
 * 配置如下:<br>
 * A.properties<br>
 * org.coodex.util.profile.NAMESPACE=XX.properties<br>
 * <br>
 * XX.properties<br>
 * abcd=xxxxxx<br>
 * <br>
 * 则相当于在A.properties中<br>
 * NAMESPACE.abcd=xxxxxx<br></S>
 * <p>
 * <p>
 * 2016-08-30
 * 1、废弃基于File类型的构造
 * 2、每个资源仅维持一个实例
 * 2016-09-05
 * 1、废弃命名空间的支持
 * 2、修改监测机制
 * Profile.reloadInterval 用以制定重新加载的间隔时间，单位为秒
 *
 * @author davidoff
 * @version v1.0 2014-03-18
 */
public class Profile {

    private static final String RELOAD_INTERVAL = System.getProperty("Profile.reloadInterval");
    private static final Logger log = LoggerFactory.getLogger(Profile.class);
    //    private static final Map<String, Profile> profiles = new HashMap<String, Profile>();
    private static final SingletonMap<String, Profile> profiles = new SingletonMap<String, Profile>(
            new SingletonMap.Builder<String, Profile>() {
                @Override
                public Profile build(String key) {
                    return new Profile(key);
                }
            }
    );
    //    private static ScheduledExecutorService RELOAD_POOL;
    private static Singleton<ScheduledExecutorService> RELOAD_POOL =
            new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newSingleThreadScheduledExecutor("profile_reload");
                }
            });
    private static Set<String> notFound = new HashSet<String>();
    protected Properties p = new Properties();
    private long lastModified = 0;
    private String resourcePath;
    private File f;
    private String location;
    private InputStream is;

    private Runnable reloadTask = new Runnable() {
        public void run() {

            try {
                if (f == null) {
                    loadFromPath(resourcePath, true);
                } else if (f.lastModified() != lastModified) {
                    lastModified = f.lastModified();
                    try {
                        is = new FileInputStream(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    load();
                }
            } finally {
                submitReloadTask();
            }
        }
    };

    protected Profile(String path) {
        this.resourcePath = path;
        loadFromPath(path);
        submitReloadTask();
    }

    private static ScheduledExecutorService getReloadPool() {
//        if (RELOAD_POOL == null) {
//            synchronized (Profile.class) {
//                if (RELOAD_POOL == null)
//                    RELOAD_POOL = ExecutorsHelper.newSingleThreadScheduledExecutor();
//            }
//        }
//        return RELOAD_POOL;
        return RELOAD_POOL.getInstance();
    }

    public static Profile getProfile(String path) {
        return profiles.getInstance(path);
//        Profile p = profiles.get(path);
//        if (p == null) {
//            p = new Profile(path);
//            profiles.put(path, p);
//        }
//        return p;
    }

//    public static InputStream getResourceAsStream(String resourcePath) throws IOException {
//        URL url = getResource(Common.trim(resourcePath, '/'));
//        return url == null ? new FileInputStream(resourcePath) : url.openStream();
//    }

//    static public URL getResource(String resource) {
//        return Common.getResource(resource);
//    }

    private void loadFromPath(String path) {
        loadFromPath(path, false);
    }

    private void submitReloadTask() {
        long inteval = -1;
        try {
            inteval = Long.parseLong(RELOAD_INTERVAL);
        } catch (Throwable t) {
        }

        if (inteval > 0) {
            ScheduledExecutorService pool = getReloadPool();
            if (pool.isShutdown() || pool.isTerminated()) return;
            pool.schedule(reloadTask, inteval, TimeUnit.SECONDS);
        }

    }

    private synchronized void loadFromPath(String path, boolean reload) {
        if (!path.startsWith("/"))
            path = "/" + path;
        URL uri = Profile.class.getResource(path);

        if (uri == null) {
            while (path.startsWith("/"))
                path = path.substring(1);

            uri = getResource(path);
        }

        if (uri != null) {
            location = uri.toString();
            if (location.indexOf('!') >= 0) {
                f = null;
                is = Profile.class.getResourceAsStream(path);
            } else {
                f = new File(uri.getFile());
                try {
                    is = new FileInputStream(f);
                    lastModified = f.lastModified();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            load();
        } else {
            if (!reload) {
                if (!notFound.contains(path)) {
                    notFound.add(path);
                    log.info("Profile [" + path + "] not found.");
                }
            }
        }
    }

//    public String getLocation() {
//        return location;
//    }

//    public Properties getProperties() {
//        return p;
//    }


    private void load() {
        try {
            p.clear();
            if (is != null) {
                p.load(is);
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean v) {
        return toBool(p.getProperty(key), v);
    }

    public String getString(String key, String v) {
        String s = p.getProperty(key);
        return s == null ? v : s;
    }


    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int v) {
        return toInt(getString(key), v);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long v) {
        return toLong(getString(key), v);
    }

    public String[] getStrList(String key) {
        return getStrList(key, ",");
    }

    public String[] getStrList(String key, String delim) {
        return getStrList(key, delim, null);
    }

    public String[] getStrList(String key, String delim, String[] v) {
        return toArray(getString(key), delim, v);
    }


}
