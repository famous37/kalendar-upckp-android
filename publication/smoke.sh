#!/usr/bin/env bash
set -euo pipefail
adb install "$RUNNER_TEMP/old-calendar/Kalendar_UPCKP_3.0_Beta1.apk"
adb install "$RUNNER_TEMP/calendar-distribution/Tserkovnyi-Kalendar-4.0.0-beta.1.apk"
packages="$(adb shell pm list packages com.shlemko.upckpcalendar | tr -d '\r')"
grep -Fxq 'package:com.shlemko.upckpcalendar' <<< "$packages"
grep -Fxq 'package:com.shlemko.upckpcalendar.beta' <<< "$packages"
adb shell am start -W -n com.shlemko.upckpcalendar.beta/com.shlemko.upckpcalendar.MainActivity
for ((i=0; i<10; i++)); do
  adb shell uiautomator dump /sdcard/calendar-ui.xml >/dev/null
  adb exec-out cat /sdcard/calendar-ui.xml > "$RUNNER_TEMP/calendar-ui.xml"
  if grep -q 'Церковний календар' "$RUNNER_TEMP/calendar-ui.xml" && grep -q 'Додати день до вибраного' "$RUNNER_TEMP/calendar-ui.xml"; then
    exit 0
  fi
  sleep 2
done
exit 1
