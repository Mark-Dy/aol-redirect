package com.iress.servlet.contextlistener;

import com.iress.common.util.FileUtil;
import com.iress.servlet.RedirectServlet;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.nio.file.*;

public class RedirectContextListener implements ServletContextListener {
    private final ConfigObserver configObserver = new ConfigObserver(FileUtil.getConfigFileLocation());

    public RedirectContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        PropertyConfigurator.configure(FileUtil.getLogFileConfig());
        configObserver.startThread();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        configObserver.stopThread();
    }

    class ConfigObserver implements Runnable {
        private volatile Thread thread;
        private final WatchService watchService;

        public ConfigObserver(String configPath) {
            Path path = Paths.get(configPath);
            try {
                watchService = path.getFileSystem().newWatchService();
                /*
                 * We are only watching for modifications
                 */
                path.register(watchService,StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void startThread() {
            thread = new Thread(this);
            thread.start();
        }

        public void stopThread() {
            if (thread != null) {
                Thread runningThread = thread;
                thread = null;
                runningThread.interrupt();
            }
        }

        @Override
        public void run() {
            Thread runningThread = Thread.currentThread();
            while (runningThread == thread) {
                WatchKey watchKey = null;

                try {
                    watchKey = watchService.take();
                    if (watchKey != null) {
                        // Check for modification
                        WatchEvent watchEvent = watchKey.pollEvents().stream()
                                .filter( e -> e.context().toString().equalsIgnoreCase( FileUtil.REDIRECT_PROPS_FILE ) )
                                .findFirst().orElse(null);

                        if(watchEvent != null) {
                            RedirectServlet.reloadConfig();
                        }
                        watchKey.reset();
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
