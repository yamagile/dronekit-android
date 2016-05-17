package org.droidplanner.android.client.interfaces;

import android.support.annotation.NonNull;

import org.droidplanner.services.android.lib.drone.connection.ConnectionParameter;
import org.droidplanner.services.android.lib.gcs.link.LinkConnectionStatus;

/**
 * An interface that will update the caller with information about the link connection.
 *
 * This is passed to the {@link org.droidplanner.android.client.Drone#connect(ConnectionParameter, LinkListener)}
 * method.
 */
public interface LinkListener {

    /**
     * The callback that notifies the caller about the current state of the link connection.
     *
     * @param connectionStatus Contains information about the connection status.
     */
    void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus);
}