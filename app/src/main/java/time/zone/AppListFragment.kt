package time.zone

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone

class AppListFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var searchView: SearchView
    private val configMap = mutableMapOf<String, String>()
    private lateinit var adapter: AppAdapter // هنوز lateinit است، اما با مقدار اولیه مدیریت می‌شود
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)

        searchView = view.findViewById(R.id.search_view)
        listView = view.findViewById(R.id.app_list)

        // تنظیم ProgressDialog
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("در حال بارگذاری لیست برنامه‌ها...")
            setCancelable(false)
        }
        progressDialog.show()

        // مقداردهی اولیه adapter با لیست خالی برای جلوگیری از خطا
        adapter = AppAdapter(requireContext(), emptyList(), configMap)
        listView.adapter = adapter

        // بارگذاری غیرهمزمان لیست اپلیکیشن‌ها
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = requireActivity().packageManager
            val apps = pm.getInstalledApplications(0).sortedBy { it.loadLabel(pm).toString() }

            withContext(Dispatchers.Main) {
                adapter = AppAdapter(requireContext(), apps, configMap) // به‌روزرسانی adapter
                listView.adapter = adapter
                progressDialog.dismiss() // بستن ProgressDialog پس از اتمام
                // به‌روزرسانی داده‌ها پس از لود اولیه
                (activity as? MainActivity)?.loadConfig()?.forEach {
                    val parts = it.split("|")
                    if (parts.size == 2) configMap[parts[0]] = parts[1]
                }
                adapter.notifyDataSetChanged()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // دیگر نیازی به فراخوانی notifyDataSetChanged در اینجا نیست، زیرا در بلوک غیرهمزمان انجام می‌شود
    }

    fun applyChanges() {
        (activity as MainActivity).saveConfig(configMap)
    }
}

class AppAdapter(
    context: android.content.Context,
    private val apps: List<android.content.pm.ApplicationInfo>,
    private val configMap: MutableMap<String, String>
) : ArrayAdapter<android.content.pm.ApplicationInfo>(context, R.layout.app_item, apps) {

    private val pm = context.packageManager
    private var filteredApps = apps.toMutableList()

    override fun getCount(): Int = filteredApps.size

    override fun getItem(position: Int): android.content.pm.ApplicationInfo = filteredApps[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.app_item, parent, false)
        val app = getItem(position)

        view.findViewById<ImageView>(R.id.app_icon).setImageDrawable(app.loadIcon(pm))
        view.findViewById<TextView>(R.id.app_name).text = app.loadLabel(pm)
        view.findViewById<TextView>(R.id.package_name).text = app.packageName

        val spinner = view.findViewById<Spinner>(R.id.timezone_spinner)
        val timeZones = listOf("") + TimeZone.getAvailableIDs().toList().sorted()

        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, timeZones)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val selectedTz = configMap[app.packageName]
        val selectedIndex = timeZones.indexOf(selectedTz ?: "")
        spinner.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val tz = parent.getItemAtPosition(pos).toString()
                if (tz.isNotEmpty()) {
                    configMap[app.packageName] = tz
                } else {
                    configMap.remove(app.packageName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    fun filter(query: String) {
        filteredApps = if (query.isEmpty()) {
            apps.toMutableList()
        } else {
            apps.filter {
                it.loadLabel(pm).toString().contains(query, true) || it.packageName.contains(query, true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}