/*
 * Copyright 2015 Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.frapontillo.pulse.crowd.fixgeomessage;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.rx.PulseSubscriber;
import com.github.frapontillo.pulse.spi.IPlugin;
import rx.Observable;
import rx.Subscriber;

/**
 * Rx {@link rx.Observable.Operator} that accepts and outputs {@link Message}s after approximating
 * its geo-location via the abstract {@link #getCoordinates(Message)} that clients should
 * implement.
 *
 * @author Francesco Pontillo
 */
public abstract class IMessageGeoFixerOperator implements Observable.Operator<Message, Message> {
    private IPlugin plugin;

    public IMessageGeoFixerOperator(IPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public Subscriber<? super Message> call(Subscriber<? super Message> subscriber) {
        return new PulseSubscriber<Message>(subscriber) {
            @Override public void onNext(Message message) {
                plugin.reportElementAsStarted(message.getId());
                message = geoFixMessage(message);
                plugin.reportElementAsEnded(message.getId());
                subscriber.onNext(message);
            }

            @Override public void onCompleted() {
                plugin.reportPluginAsCompleted();
                super.onCompleted();
            }

            @Override public void onError(Throwable e) {
                plugin.reportPluginAsErrored();
                super.onError(e);
            }
        };
    }

    /**
     * Fixes the geo-location of a {@link Message} by delegating the fixing to {@link
     * #getCoordinates(Message)}.
     *
     * @param message The {@link Message} to fix.
     *
     * @return The fixed {@link Message}.
     */
    protected Message geoFixMessage(Message message) {
        Double[] coordinates = getCoordinates(message);
        if (coordinates != null && coordinates.length == 2) {
            message.setLatitude(coordinates[0]);
            message.setLongitude(coordinates[1]);
        }
        return message;
    }

    /**
     * Actual coordinates geo-fixer for the input {@link Message}.
     *
     * @param message The {@link Message} to retrieve geo-coordinates for.
     *
     * @return An array of {@link Double} representing, in order, latitude and longitude.
     */
    public abstract Double[] getCoordinates(Message message);

}
