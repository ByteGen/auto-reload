package com.bytegen.common.reload.core;

import com.bytegen.common.reload.event.EventPublisher;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * The watching process does not start by default, initiation is triggered by calling <code>startWatching()</code>
 */
public class PropertiesFileWatcher {
    private static Logger log = LoggerFactory.getLogger(PropertiesFileWatcher.class);

    private final EventPublisher eventPublisher;

    private final Set<EncodedResource> locations;
    private WatchService watchService;
    private final ExecutorService service;

    public PropertiesFileWatcher(final Set<EncodedResource> locations, final EventPublisher eventPublisher) throws IOException {
        this.locations = locations;
        this.eventPublisher = eventPublisher;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.service = Executors.newCachedThreadPool();
    }

    public void startWatching() {
        if (null == this.eventPublisher) {
            throw new BeanInitializationException("Event publisher not setup, you should not be calling this method...!");
        }

        final Map<Path, List<EncodedResource>> pathsAndResources = findAvailableResourcePaths();
        for (final Path pathToWatch : pathsAndResources.keySet()) {
            final List<EncodedResource> availableResources = pathsAndResources.get(pathToWatch);
            log.debug("Starting ResourceWatcher on file {}", availableResources);
            this.service.submit(new ResourceWatcher(pathToWatch, availableResources));
        }
    }

    public void stop() {
        try {
            log.debug("Closing File Watching Service");
            this.watchService.close();

            log.debug("Shuting down Thread Service");
            this.service.shutdownNow();
        } catch (final IOException e) {
            log.error("Unable to stop file watcher", e);
        }
    }

    private Map<Path, List<EncodedResource>> findAvailableResourcePaths() {
        final Map<Path, List<EncodedResource>> map = Maps.newHashMap();
        for (final EncodedResource resource : this.locations) {
            final Path resourceParentPath = getResourceParentPath(resource.getResource());
            map.computeIfAbsent(resourceParentPath, k -> new ArrayList<>());
            map.get(resourceParentPath).add(resource);
        }
        return map;
    }

    private Path getResourceParentPath(final Resource resource) {
        try {
            return Paths.get(resource.getFile()
                    .getParentFile()
                    .toURI());
        } catch (final IOException e) {
            log.error("Unable to get resource path", e);
        }
        return null;
    }

    private void publishResourceChangedEvent(final EncodedResource resource) {
        this.eventPublisher.onResourceChanged(resource);
    }

    private WatchService getWatchService() {
        return this.watchService;
    }


    private class ResourceWatcher implements Runnable {

        private final Path path;
        private final List<EncodedResource> resources;

        public ResourceWatcher(final Path path, final List<EncodedResource> resources) {
            this.path = path;
            this.resources = resources;
        }

        @Override
        public void run() {
            try {
                log.debug("START");
                log.debug("Watching for modification events for path {}", this.path.toString());
                while (!Thread.currentThread()
                        .isInterrupted()) {
                    final WatchKey pathBeingWatched = this.path.register(getWatchService(), ENTRY_MODIFY);

                    WatchKey watchKey = null;
                    try {
                        watchKey = getWatchService().take();
                    } catch (final ClosedWatchServiceException | InterruptedException e) {
                        log.debug("END");
                        Thread.currentThread().interrupt();
                    }

                    if (watchKey != null) {
                        for (final WatchEvent<?> event : pathBeingWatched.pollEvents()) {
                            log.debug("File modification Event Triggered");
                            final Path target = path(event.context());
                            if (isValidTargetFile(target)) {
                                final Path watchedPath = path(watchKey.watchable());
                                final Kind<?> eventKind = event.kind();

                                logNewEvent(watchedPath, eventKind, target);
                                publishResourceChangedEvent(getResource(target));
                            }
                        }
                        if (!watchKey.reset()) {
                            log.debug("END");
                            Thread.currentThread()
                                    .interrupt();
                            return;
                        }
                    }
                }
            } catch (final Exception e) {
                log.error("Exception thrown when watching resources, path {}\nException:", this.path.toString(), e.getMessage());
                stop();
            }
        }

        private void logNewEvent(final Path watchedPath, final Kind<?> eventKind, final Path target) {
            log.debug("Watched Resource changed, modified file [{}]", target.getFileName()
                    .toString());
            log.debug("  Event Kind [{}]", eventKind);
            log.debug("      Target [{}]", target);
            log.debug("Watched Path [{}]", watchedPath);
        }

        private Path path(final Object object) {
            return (Path) object;
        }

        private boolean isValidTargetFile(final Path target) {
            for (final EncodedResource resource : this.resources) {
                if (pathMatchesResource(target, resource)) {
                    return true;
                }
            }
            return false;
        }

        public EncodedResource getResource(final Path target) {
            for (final EncodedResource resource : this.resources) {
                if (pathMatchesResource(target, resource)) {
                    return resource;
                }
            }
            return null;
        }

        private boolean pathMatchesResource(final Path target, final EncodedResource resource) {
            return target.getFileName()
                    .toString()
                    .equals(resource.getResource().getFilename());
        }
    }
}
