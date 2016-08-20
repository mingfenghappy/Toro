/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.toro.ext;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import im.ene.lab.toro.Toro;

/**
 * Created by eneim on 6/6/16.
 *
 * {@since 2.0} Main class for Toro's extensions.
 */
// Disable for now
/* public */ final class ToroExt {

  volatile static ToroExt sInstance;

  private final Application app;
  private int flags;  // to mask our support keys

  // Extensions
  YouTube youTube;

  private ToroExt(Application app) {
    this.app = app;
    this.flags = 0x00000000;
  }

  // We will ask for Youtube API Key by Application's meta tag
  public static ToroExt with(Application app) {
    if (sInstance == null) {
      synchronized (ToroExt.class) {
        sInstance = new ToroExt(app);
      }
    }

    return sInstance;
  }

  /**
   * Add support to YouTube Player API
   *
   * @return current {@link ToroExt} instance.
   */
  /* package */ final ToroExt youtube() {
    // 0. Do not allow initializing YouTube twice
    if ((this.flags & Extensions.YOUTUBE) == Extensions.YOUTUBE) {
      throw new IllegalStateException(
          "YouTube support has already been applied for this Application.");
    }

    // Apply mask
    this.flags |= Extensions.YOUTUBE;

    // 1. Check if there is R.string.google_api_key (generated by Google Service plugin).
    String apiKey = null;
    int apiKeyStringId =
        app.getResources().getIdentifier("google_api_key", "string", app.getPackageName());
    if (apiKeyStringId != 0) {
      apiKey = app.getString(apiKeyStringId);
    }

    // 2. Obtain from META definition.
    if (apiKey == null) {
      try {
        ApplicationInfo appInfo = app.getPackageManager()
            .getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA);
        String ytMetaName = "toro.ext.youtube.API_KEY";
        apiKey = appInfo.metaData.getString(ytMetaName);
      } catch (PackageManager.NameNotFoundException | NullPointerException error) {
        error.printStackTrace();
      }
    }

    if (apiKey == null) {
      throw new RuntimeException("A valid YouTube API Key is required.");
    }

    this.youTube = new YouTube(apiKey);
    return this;
  }

  /**
   * Globally initialize ToroExt.
   */
  public void init() {
    Toro.init(this.app);
  }
}
