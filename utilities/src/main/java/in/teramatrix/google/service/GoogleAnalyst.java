package in.teramatrix.google.service;

import android.app.Application;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * At the moment, this class is only facilitated with only four major Google Analytics services. That means class is able to
 * send screen names, actions, custom dimensions and exceptions. To execute the operation, you have to pass a {@link Tracker} in every static method.
 * Here I'm giving the definition of the method that will help you to get {@link Tracker}, just place it in {@link Application} class.
 *
 * <pre>
 * private Tracker mTracker;
 * synchronized public Tracker getDefaultTracker() {
 *      if (mTracker == null) {
 *          GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
 *          // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
 *          mTracker = analytics.newTracker(R.xml.global_tracker);
 *          mTracker.enableExceptionReporting(true);
 *      }
 *      return mTracker;
 * }
 * </pre>
 * <br/>
 * For more details : <a href="https://developers.google.com/analytics/devguides/collection/android/v4/#set-up-your-project">Add Analytics to Your Android App</a>
 *
 * @author Mohsin Khan
 * @date 3/23/2016
 */
@SuppressWarnings({"unused", "ConfusingArgumentToVarargsMethod"})
public class GoogleAnalyst {

    private static final String APPLICATION = "in.teramatrix.flint.driver.FlintDriver";

    /**
     * This method will send the name of current visible screen to the analytics.
     * <br/>
     * See <a href="https://developers.google.com/analytics/devguides/collection/android/v4/screens#overview">Screens</a>
     * @param screen name of the screen that will be on Google Analytics
     */
    public static void sendScreenName(Application application, String screen) {
        if (application != null) {
            try {
                Object object = Class.forName(APPLICATION).cast(application);
                Method method = object.getClass().getMethod("getTracker", null);
                Tracker tracker = (Tracker) method.invoke(object, null);
                if (tracker != null) {
                    tracker.setScreenName(screen);
                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will send action/event to the analytics.
     * <br/>
     * See <a href="https://developers.google.com/analytics/devguides/collection/android/v4/events#overview">Event Tracking</a>
     * @param category name of event/action category that will be on Google Analytics
     * @param action name of action like what is user doing right now for example "watching gallery"
     */
    public static void sendEvent(Application application, String category, String action) {
        if (application != null) {
            try {
                Object object = Class.forName(APPLICATION).cast(application);
                Method method = object.getClass().getMethod("getTracker", null);
                Tracker tracker = (Tracker) method.invoke(object, null);
                if (tracker != null) {
                    tracker.send(
                            new HitBuilders.EventBuilder()
                                    .setCategory(category)
                                    .setAction(action)
                                    .build()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will send screen name and custom dimensions to the analytics. To accomplish task just pass a {@link HashMap<>}
     * of key value pair. For example consider following, here we are sending user's information to the analytics.
     * <br/><br/>
     * {@code HashMap<String, String> dimensions = new HashMap<String, String>();}
     * <br/>
     * {@code dimensions.put("&cd1", userId);}
     * <br/>
     * {@code dimensions.put("&cd2", email);}
     * <br/>
     * {@code dimensions.put("&cd3", phone);}
     * <br/>
     * {@code dimensions.put("&cd4", timezone);}
     * <br/>
     * For more details : <a href="https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cd_">Custom Dimension</a>
     * @param screen name of the screen that will be on Google Analytics
     * @param dimensions a map of key(custom dimension index)-value pair
     */
    public static void sendCustomDimension(Application app, String screen, HashMap<String, String> dimensions) {
        if (app != null) {
            try {
                Object object = Class.forName(APPLICATION).cast(app);
                Method method = object.getClass().getMethod("getTracker", null);
                Tracker tracker = (Tracker) method.invoke(object, null);
                if (tracker != null) {
                    tracker.setScreenName(screen);

                    //Adding all keys and their values to tracker
                    ArrayList<String> keys = new ArrayList<>(dimensions.keySet());
                    for (String key : keys)
                        tracker.set(key, dimensions.get(key));

                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will send {@link Exception} to google analytics.
     * @param application current instance of {@link Application} class
     * @param exception exception to be sent
     */
    public static void sendException(Application application, Exception exception) {
        if (application != null) {
            try {
                Object object = Class.forName(APPLICATION).cast(application);
                Method method = object.getClass().getMethod("getTracker", null);
                Tracker tracker = (Tracker) method.invoke(object, null);
                if (tracker != null) {
                    String description = new StandardExceptionParser(application.getApplicationContext(), null)
                            .getDescription(Thread.currentThread().getName(), exception);
                    tracker.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(description)
                            .setFatal(false)
                            .build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
