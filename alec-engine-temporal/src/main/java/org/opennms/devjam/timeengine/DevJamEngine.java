package org.opennms.devjam.timeengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.opennms.alec.datasource.api.Alarm;
import org.opennms.alec.datasource.api.AlarmFeedback;
import org.opennms.alec.datasource.api.InventoryObject;
import org.opennms.alec.datasource.api.Severity;
import org.opennms.alec.datasource.api.Situation;
import org.opennms.alec.datasource.api.SituationHandler;
import org.opennms.alec.datasource.common.ImmutableSituation;
import org.opennms.alec.engine.api.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevJamEngine implements Engine {

    private static final Logger LOG = LoggerFactory.getLogger(DevJamEngine.class);

    int windowSizeMillis = 10000;

    SituationHandler handler;

    List<Situation> situations = new ArrayList<Situation>();

    // Initial hard-coded default of 10 seconds.
    int sliceMillis = 10000; // time slice size in millis

    long windowStart = 0;

    long windowStop = 0;

    ImmutableSituation.Builder current;

    @Override
    public void onAlarmCreatedOrUpdated(Alarm alarm) {
        if (isInCurrentWindow(alarm)) {
            LOG.info("Alarm Received: {}", alarm);
            current.addAlarm(alarm);
        } else {
            beginNewWindow(alarm);
        }
    }

    @Override
    public void onAlarmCleared(Alarm alarm) {
        if (isInCurrentWindow(alarm)) {
            LOG.info("Alarm Cleared: {}", alarm);
            current.addAlarm(alarm);
            current.setSeverity(Severity.CLEARED);
        } else {
            beginNewWindow(alarm);
        }
    }

    private boolean isInCurrentWindow(Alarm alarm) {
        return alarm.getTime() >= windowStart && alarm.getTime() < windowStop;
    }

    // Send the last Situation, start a new one and a new Window
    private void beginNewWindow(Alarm alarm) {
        LOG.info("Beginging new window with: {}", alarm);
        if (current != null) {
            LOG.info("Closing and Sending current Situation: {}", current.build());
            handler.onSituation(current.build());
        }
        windowStart = alarm.getTime();
        windowStop = windowStart + sliceMillis;
        current = ImmutableSituation.newBuilder();
        current.setId(UUID.randomUUID().toString());
        current.setCreationTime(windowStart);
        current.addAlarm(alarm);
        current.setSeverity(alarm.getSeverity());
    }

    @Override
    public void registerSituationHandler(SituationHandler handler) {
        this.handler = handler;
    }

    public void setSliceMillis(int sliceMillis) {
        this.sliceMillis = sliceMillis;
    }

    @Override
    public void init(List<Alarm> alarms, List<AlarmFeedback> alarmFeedback, List<Situation> situations, List<InventoryObject> inventory) {
        // TODO
    }

    @Override
    public long getTickResolutionMs() {
        return sliceMillis;
    }

    @Override
    public void tick(long timestampInMillis) {
        if (current != null) {
            handler.onSituation(current.build());
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void onInventoryAdded(Collection<InventoryObject> inventory) {
        // pass
    }

    @Override
    public void onInventoryRemoved(Collection<InventoryObject> inventory) {
        // pass
    }

    @Override
    public void handleAlarmFeedback(AlarmFeedback alarmFeedback) {
        // pass
    }

    @Override
    public void deleteSituation(String situationId) {
        // pass
    }

}
