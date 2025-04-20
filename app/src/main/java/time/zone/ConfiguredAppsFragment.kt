package time.zone

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.io.File
import kotlin.concurrent.thread

class ConfiguredAppsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: ConfiguredAppAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configured_apps, container, false)
        listView = view.findViewById(R.id.configured_list)
        adapter = ConfiguredAppAdapter(requireContext(), mutableListOf())
        listView.adapter = adapter
        registerForContextMenu(listView)
        loadConfig()
        return view
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == R.id.configured_list) {
            menu.add(0, 1, 0, "حذف")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
            val selectedEntry = adapter.getItem(info.position) as ConfiguredApp
            deleteConfig(selectedEntry.packageName)
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun loadConfig() {
        val pm = requireActivity().packageManager
        val installedApps = pm.getInstalledApplications(0).associateBy { it.packageName }
        val configList = (activity as MainActivity).loadConfig().mapNotNull { config ->
            val parts = config.split("|")
            if (parts.size == 2) {
                val packageName = parts[0]
                val timeZone = parts[1]
                val appInfo = installedApps[packageName]
                if (appInfo != null) {
                    ConfiguredApp(appInfo, packageName, timeZone)
                } else {
                    null
                }
            } else {
                null
            }
        }
        adapter.clear()
        adapter.addAll(configList)
        adapter.notifyDataSetChanged()
    }

    private fun deleteConfig(packageName: String) {
        thread {
            val configFile = File("/data/timezone_config.txt")
            if (configFile.exists()) {
                val currentConfig = configFile.readText()
                val updatedConfig = currentConfig.lines()
                    .filter { !it.startsWith("$packageName|") }
                    .joinToString("\n")
                val writeCommand = arrayOf("su", "-c", "echo -n \"$updatedConfig\" > /data/timezone_config.txt")
                val process = Runtime.getRuntime().exec(writeCommand)
                process.waitFor()
                if (process.exitValue() == 0) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "مورد حذف شد", Toast.LENGTH_SHORT).show()
                        loadConfig()
                    }
                }
            }
        }
    }
}

data class ConfiguredApp(val appInfo: ApplicationInfo, val packageName: String, val timeZone: String)

class ConfiguredAppAdapter(
    context: android.content.Context,
    private val apps: MutableList<ConfiguredApp>
) : ArrayAdapter<ConfiguredApp>(context, R.layout.configured_app_item, apps) {

    private val pm = context.packageManager

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.configured_app_item, parent, false)
        val app = getItem(position)!!
        view.findViewById<ImageView>(R.id.app_icon).setImageDrawable(app.appInfo.loadIcon(pm))
        view.findViewById<TextView>(R.id.app_name).text = app.appInfo.loadLabel(pm)
        view.findViewById<TextView>(R.id.package_name).text = app.packageName
        view.findViewById<TextView>(R.id.timezone_text).text = app.timeZone
        return view
    }
}