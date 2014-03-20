
package com.android.settings.rascarlo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    // General
    private static String STATUS_BAR_GENERAL_CATEGORY = "status_bar_general_category";
    // Brightness control
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
 // Double-tap to sleep
    private static final String DOUBLE_TAP_SLEEP_GESTURE = "double_tap_sleep_gesture";
    // Network Traffic
    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
    private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
	// Network Stats
private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
    // Status bar battery style
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    // Native battery percentage
    private static final String STATUS_BAR_NATIVE_BATTERY_PERCENTAGE = "status_bar_native_battery_percentage";
    // Clock
    private static final String STATUS_BAR_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    // Quick Settings
    private static final String QUICK_SETTINGS_CATEGORY = "status_bar_quick_settings_category";
    private static final String QUICK_PULLDOWN = "quick_pulldown";

    // General
    private PreferenceCategory mStatusBarGeneralCategory;
    // Status bar battery style
    private ListPreference mStatusBarBattery;
    // Native battery percentage
    private ListPreference mStatusBarNativeBatteryPercentage;
    // Brightness control
    private CheckBoxPreference mStatusBarBrightnessControl;
    // Double-tap to sleep
    private CheckBoxPreference mStatusBarDoubleTapSleepGesture;
    // Clock
    private ListPreference mStatusBarAmPm;
    private CheckBoxPreference mStatusBarClock;
    // Quick Settings
    private ListPreference mQuickPulldown;
    // Notification Count
    private static final String STATUSBAR_NOTIF_COUNT = "status_bar_notif_count";
    // Network Traffic
    private ListPreference mNetTrafficState;
    private ListPreference mNetTrafficUnit;
    private ListPreference mNetTrafficPeriod;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private CheckBoxPreference mStatusBarNotifCount;
    private int mNetTrafficVal;
    private int MASK_UP;
    private int MASK_DOWN;
    private int MASK_UNIT;
    private int MASK_PERIOD;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);
	PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        loadResources();


            // General category
            mStatusBarGeneralCategory = (PreferenceCategory) findPreference(STATUS_BAR_GENERAL_CATEGORY);
            mStatusBarBrightnessControl = (CheckBoxPreference) getPreferenceScreen().findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
            // only show on phones
            if (!Utils.isPhone(getActivity())) {
                mStatusBarGeneralCategory.removePreference(mStatusBarBrightnessControl);
                getPreferenceScreen().removePreference((PreferenceCategory) findPreference(STATUS_BAR_GENERAL_CATEGORY));
            } else {
                // Status bar brightness control
                mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), 
                        Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
                try {
                    if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                        mStatusBarBrightnessControl.setEnabled(false);
                        mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                    }
                } catch (SettingNotFoundException e) {
                }
            }

 // Status bar double-tap to sleep
            mStatusBarDoubleTapSleepGesture = (CheckBoxPreference) getPreferenceScreen().findPreference(DOUBLE_TAP_SLEEP_GESTURE);
            mStatusBarDoubleTapSleepGesture.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, 0) == 1));

            // Status bar battery style
            mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY);
            mStatusBarBattery.setOnPreferenceChangeListener(this);
            int batteryStyleValue = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY, 0);
            mStatusBarBattery.setValue(String.valueOf(batteryStyleValue));
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());

	    // Notification Count
 	    mStatusBarNotifCount = (CheckBoxPreference) findPreference(STATUSBAR_NOTIF_COUNT);
            mStatusBarNotifCount.setOnPreferenceChangeListener(this);

            // Network Traffic
            mNetTrafficState = (ListPreference) getPreferenceScreen().findPreference(NETWORK_TRAFFIC_STATE);
            mNetTrafficUnit = (ListPreference) getPreferenceScreen().findPreference(NETWORK_TRAFFIC_UNIT);
            mNetTrafficPeriod = (ListPreference) getPreferenceScreen().findPreference(NETWORK_TRAFFIC_PERIOD);

            // TrafficStats will return UNSUPPORTED if the device does not support it.
            if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                    TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
                mNetTrafficVal = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, 0);
                int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
                intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
                if (intIndex <= 0) {
                    mNetTrafficUnit.setEnabled(false);
                    mNetTrafficPeriod.setEnabled(false);
                }
                mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
                mNetTrafficState.setSummary(mNetTrafficState.getEntry());
                mNetTrafficState.setOnPreferenceChangeListener(this);

                mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
                mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
                mNetTrafficUnit.setOnPreferenceChangeListener(this);

                intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
                intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
                mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
                mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
                mNetTrafficPeriod.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(findPreference(NETWORK_TRAFFIC_STATE));
                getPreferenceScreen().removePreference(findPreference(NETWORK_TRAFFIC_UNIT));
                getPreferenceScreen().removePreference(findPreference(NETWORK_TRAFFIC_PERIOD));
            }

 		// Network Stats
	
	mStatusBarNetworkActivity = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
	mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
	Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
	mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);

            // Clock
            mStatusBarClock = (CheckBoxPreference) getPreferenceScreen().findPreference(STATUS_BAR_CLOCK);
            mStatusBarClock.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK, 1) == 1));
            // Am-Pm
            mStatusBarAmPm = (ListPreference) getPreferenceScreen().findPreference(STATUS_BAR_AM_PM);
            try {
                if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.TIME_12_24) == 24) {
                    mStatusBarAmPm.setEnabled(false);
                    mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
                }
            } catch (SettingNotFoundException e ) {
            }

            int statusBarAmPm = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_AM_PM, 2);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);

            // Quick settings category
            // Quick Settings pull down
            mQuickPulldown = (ListPreference) getPreferenceScreen().findPreference(QUICK_PULLDOWN);
            // only show on phones
            if (!Utils.isPhone(getActivity())) {
                if (mQuickPulldown != null)
                    getPreferenceScreen().removePreference(mQuickPulldown);
                getPreferenceScreen().removePreference((PreferenceCategory) findPreference(QUICK_SETTINGS_CATEGORY));
            } else {
                mQuickPulldown.setOnPreferenceChangeListener(this);
                int quickPulldownValue = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), 
                        Settings.System.QS_QUICK_PULLDOWN, 0);
                mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
                mQuickPulldown.setSummary(mQuickPulldown.getEntry());
            }
}

    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mStatusBarBattery) {
            int batteryStyleValue = Integer.valueOf((String) objValue);
            int batteryStyleIndex = mStatusBarBattery.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY, batteryStyleValue);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[batteryStyleIndex]);
            updateStatusBarNativeBatteryPercentage();
            return true;

        } else if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) objValue);
            int indexAmPm = mStatusBarAmPm.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[indexAmPm]);
            return true;

	} else if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;

        } else if (preference == mNetTrafficState) {
            int intState = Integer.valueOf((String)objValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficState.findIndexOfValue((String) objValue);
            mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
            if (intState == 0) {
                mNetTrafficUnit.setEnabled(false);
                mNetTrafficPeriod.setEnabled(false);
            } else {
                mNetTrafficUnit.setEnabled(true);
                mNetTrafficPeriod.setEnabled(true);
            }
        } else if (preference == mNetTrafficUnit) {
            // 1 = Display as Byte/s; default is bit/s
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String)objValue).equals("1"));
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficUnit.findIndexOfValue((String) objValue);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
        } else if (preference == mNetTrafficPeriod) {
            int intState = Integer.valueOf((String)objValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficPeriod.findIndexOfValue((String) objValue);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);

        } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) objValue);
            int quickPulldownIndex = mQuickPulldown.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.QS_QUICK_PULLDOWN, quickPulldownValue);
            mQuickPulldown.setSummary(mQuickPulldown.getEntries()[quickPulldownIndex]);
            return true;

	} else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
	return true;

        }
        return false;
    }

    private void loadResources() {
        Resources resources = getActivity().getResources();
        MASK_UP = resources.getInteger(R.integer.maskUp);
        MASK_DOWN = resources.getInteger(R.integer.maskDown);
        MASK_UNIT = resources.getInteger(R.integer.maskUnit);
        MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
    }

    // intMask should only have the desired bit(s) set
    private int setBit(int intNumber, int intMask, boolean blnState) {
        if (blnState) {
            return (intNumber | intMask);
        }
        return (intNumber & ~intMask);
    }

    private boolean getBit(int intNumber, int intMask) {
        return (intNumber & intMask) == intMask;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mStatusBarBrightnessControl) {
            value = mStatusBarBrightnessControl.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
            return true;

        } else if (preference == mStatusBarDoubleTapSleepGesture) {
            value = mStatusBarDoubleTapSleepGesture.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, value ? 1: 0);
            return true;

        } else if (preference == mStatusBarClock) {
            value = mStatusBarClock.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateStatusBarNativeBatteryPercentage() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0) == 0) {
            mStatusBarNativeBatteryPercentage.setEnabled(true);
        } else {
            mStatusBarNativeBatteryPercentage.setEnabled(false);
        }
    }
}
