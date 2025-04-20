package time.zone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File
import kotlin.concurrent.thread

interface ApiService {
    @GET("json")
    suspend fun getTimeZone(): TimeZoneResponse
}

data class TimeZoneResponse(
    val time_zone: String
)

class MainActivity : AppCompatActivity() {
    private val configPath = "/data/timezone_config.txt"
    private val scriptPath = "/data/adb/service.d/timezone.sh"
    private lateinit var viewPager: ViewPager2
    private var pressedTime: Long = 0
    private lateinit var timezoneEditText: EditText // نوع صریح تعریف شده
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ipleak.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService = retrofit.create(ApiService::class.java)

    companion object {
        private const val REQUEST_PERMISSIONS = 100
        private const val REQUEST_MANAGE_STORAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        requestPermissions()

        thread {
            if (!isRooted()) {
                runOnUiThread {
                    Toast.makeText(this, "دستگاه باید روت شده با Magisk باشه!", Toast.LENGTH_LONG).show()
                    finish()
                }
                return@thread
            }

            setupBootScript()
            runOnUiThread {
                setupUI()
                fetchTimeZone()
            }
        }
    }

    private fun setupUI() {
        viewPager = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        viewPager.adapter = PagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "انتخاب اپ‌ها" else "تایم‌زون‌های تنظیم‌شده"
        }.attach()
    }

    private fun setupBootScript() {
        if (!File(scriptPath).exists()) {
            thread {
                val inputStream = resources.openRawResource(R.raw.timezone)
                val scriptContent = inputStream.bufferedReader().use { it.readText() }

                val writeCommand = arrayOf("su", "-c", "echo -n \"$scriptContent\" > $scriptPath")
                val writeProcess = Runtime.getRuntime().exec(writeCommand)
                writeProcess.waitFor()

                val chmodCommand = arrayOf("su", "-c", "chmod +x $scriptPath")
                val chmodProcess = Runtime.getRuntime().exec(chmodCommand)
                chmodProcess.waitFor()

                if (writeProcess.exitValue() == 0 && chmodProcess.exitValue() == 0) {
                    runOnUiThread {
                        Toast.makeText(this, "اسکریپت بوت نصب شد", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun saveConfig(configMap: Map<String, String>) {
        thread {
            val configFile = File(configPath)
            val currentConfig = if (configFile.exists()) configFile.readText() else ""
            val updatedLines = currentConfig.lines().filter { it.isNotBlank() }.associate {
                val parts = it.split("|")
                parts[0] to parts[1]
            }.toMutableMap()
            updatedLines.putAll(configMap)

            val updatedConfig = updatedLines.map { "${it.key}|${it.value}" }.joinToString("\n")
            val writeCommand = arrayOf("su", "-c", "echo -n \"$updatedConfig\" > $configPath")
            val process = Runtime.getRuntime().exec(writeCommand)
            process.waitFor()
            if (process.exitValue() == 0) {
                runOnUiThread {
                    Toast.makeText(this, "تنظیمات ذخیره شدند", Toast.LENGTH_SHORT).show()
                    showRebootDialog()
                }
            }
        }
    }

    fun loadConfig(): List<String> {
        val configFile = File(configPath)
        return if (configFile.exists()) {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $configPath"))
            process.waitFor()
            process.inputStream.bufferedReader().readText().trim().lines().filter { it.isNotBlank() }
        } else emptyList()
    }

    private fun isRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            process.waitFor()
            process.inputStream.bufferedReader().readText().contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "مجوزها تأیید شدند", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "دسترسی به حافظه تأیید شد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val timezoneItem = menu.findItem(R.id.action_timezone) // مقداردهی درست
        timezoneEditText = timezoneItem.actionView?.findViewById(R.id.timezone_edittext) ?: throw IllegalStateException("Timezone EditText not found")
        return true
    }

    private fun fetchTimeZone() {
        lifecycleScope.launch {
            try {
                val response = apiService.getTimeZone()
                timezoneEditText.setText(response.time_zone)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "خطا در دریافت تایم‌زون", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_apply -> {
                val fragment = supportFragmentManager.fragments[0] as? AppListFragment
                fragment?.applyChanges()
                true
            }
            R.id.action_reboot -> {
                showRebootDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRebootDialog() {
        AlertDialog.Builder(this)
            .setTitle("تأیید ری‌استارت")
            .setMessage("برای اعمال تغییرات، دستگاه باید ری‌استارت شود. ادامه می‌دهید؟")
            .setPositiveButton("بله") { _, _ ->
                Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
            }
            .setNegativeButton("خیر", null)
            .show()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 1) {
            viewPager.currentItem = 0
        } else {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
                finish()
            } else {
                Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            pressedTime = System.currentTimeMillis()
        }
    }
}