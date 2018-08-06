package com.bytegen.common.reload.event;

import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.google.common.eventbus.EventBus;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class GuavaEventNotifier implements EventNotifier {

    private static class Holder {
        static final GuavaEventNotifier INSTANCE = new GuavaEventNotifier();
    }

    public static GuavaEventNotifier getInstance() {
        return Holder.INSTANCE;
    }

    private final EventBus guavaEvent = new EventBus("auto_reload_properties");

    private GuavaEventNotifier() {
    }

    @Override
    public void post(final PropertyChangedEvent event) {
        guavaEvent.post(event);
    }

    @Override
    public void unregister(final EventSubscriber eventSubscriber) {
        guavaEvent.unregister(eventSubscriber);
    }

    @Override
    public void register(final EventSubscriber eventSubscriber) {
        guavaEvent.register(eventSubscriber);
    }

}
