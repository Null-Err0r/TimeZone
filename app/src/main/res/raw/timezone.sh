#!/system/bin/sh
MODDIR=${0%/*}

# منتظر بوت کامل شدن
until [ "$(getprop sys.boot_completed)" = "1" ]; do
    sleep 1
done

# حلقه برای چک کردن دوره‌ای
while true; do
    if [ -f "/data/timezone_config.txt" ]; then
        while IFS='|' read -r package timezone; do
            if [ -n "$package" ] && [ -n "$timezone" ]; then
                pid=$(pidof "$package")
                if [ -n "$pid" ]; then
                    nsenter --target "$pid" --mount --uts --ipc --net --pid sh -c "setprop persist.sys.timezone '$timezone'"
                fi
            fi
        done < "/data/timezone_config.txt"
    fi
    sleep 5
done
