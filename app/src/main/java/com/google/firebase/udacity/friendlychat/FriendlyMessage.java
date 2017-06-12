/**
 * Copyright Google Inc. All Rights Reserved.
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
package com.google.firebase.udacity.friendlychat;

public class FriendlyMessage {

    private String timeReceived;
    private String timeSent;
    private String photoUrl;

    public FriendlyMessage() {
    }


    public FriendlyMessage(String timeSent, String timeReceived, String photoUrl) {
        this.timeSent = timeSent;
        this.timeReceived = timeReceived;
        this.photoUrl = photoUrl;
    }


    public String getTimeReceived() {
        return timeReceived;
    }

    public void setText(String timeReceived) {
        this.timeReceived = timeReceived;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setName(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
