package time.zone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.io.File

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val configFile = File("/data/timezone_config.txt")
            if (configFile.exists()) {
                Toast.makeText(context, "دستگاه بوت شد، تنظیمات تایم‌زون اعمال می‌شوند.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}