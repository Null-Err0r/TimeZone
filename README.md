# تنظیم‌کننده تایم‌زون TimeZone

 یک اپلیکیشن اندرویدی است که به کاربران امکان می‌دهد تایم‌زون‌های سفارشی را برای برنامه‌های جداگانه روی دستگاه‌های اندرویدی روت‌شده تنظیم کنند. این قابلیت برای برنامه‌هایی که به تایم‌زون خاصی برای عملکرد خود وابسته هستند بسیار مفید است و به کاربران اجازه می‌دهد تایم‌زون سیستمی را برای هر برنامه به‌صورت جداگانه تغییر دهند.
 
ویژگی‌ها

انتخاب برنامه‌ها: مرور و انتخاب برنامه‌های نصب‌شده برای تنظیم تایم‌زون آن‌ها.

تایم‌زون‌های سفارشی: تخصیص تایم‌زون‌های خاص به برنامه‌ها از فهرست جامع تایم‌زون‌های موجود.

جستجوی آسان: جستجوی سریع برنامه‌ها بر اساس نام یا نام پکیج.

نمایش برنامه‌های تنظیم‌شده: مشاهده و مدیریت فهرست برنامه‌هایی که تایم‌زون سفارشی دارند.

حذف تنظیمات: حذف تنظیمات تایم‌زون برای برنامه‌های خاص از طریق منوی زمینه‌ای.

پایداری پس از بوت: اعمال خودکار تنظیمات تایم‌زون هنگام بوت دستگاه با استفاده از اسکریپت بوت (نیاز به روت).

تشخیص تایم‌زون از شبکه: دریافت تایم‌زون فعلی از یک API خارجی برای کمک به تنظیمات.

اعلان ری‌استارت: نمایش اعلان برای ری‌استارت دستگاه جهت اعمال تغییرات.



ساخت اپلیکیشن:

پروژه را در Android Studio باز کنید.
با استفاده از گزینه Build > Build Bundle(s) / APK(s) > Build APK(s) فایل APK را بسازید.


استفاده

اجرای اپلیکیشن:
اپلیکیشن را روی دستگاه روت‌شده باز کنید. اگر دستگاه روت نشده باشد، اپلیکیشن خطایی نمایش داده و بسته می‌شود.


انتخاب برنامه‌ها:

به تب "انتخاب برنامه‌ها" بروید.

از نوار جستجو برای یافتن برنامه‌های خاص استفاده کنید.

تایم‌زون مورد نظر را از منوی کشویی کنار هر برنامه انتخاب کنید.



اعمال تغییرات:

دکمه "اعمال" را از منو فشار دهید تا تنظیمات ذخیره شوند.

اعلانی برای ری‌استارت دستگاه جهت اعمال تغییرات نمایش داده می‌شود.



مشاهده برنامه‌های تنظیم‌شده:

به تب "تایم‌زون‌های تنظیم‌شده" بروید تا برنامه‌هایی که تایم‌زون سفارشی دارند را ببینید.

برای حذف تنظیمات یک برنامه، روی آن طولانی فشار دهید.



اسکریپت بوت:

در اولین اجرا، اپلیکیشن یک اسکریپت بوت (/data/adb/service.d/timezone.sh) نصب می‌کند تا تنظیمات پس از ری‌استارت حفظ شوند.

این اسکریپت هنگام بوت دستگاه به‌صورت خودکار اجرا می‌شود (نیاز به Magisk).




جزئیات فنی

دسترسی روت: 
از دستورات su برای خواندن/نوشتن فایل‌های تنظیمات (/data/timezone_config.txt) و نصب اسکریپت بوت استفاده می‌کند.

ذخیره‌سازی تنظیمات:
 تنظیمات تایم‌زون برنامه‌ها در فایل /data/timezone_config.txt با فرمت packageName|timeZone ذخیره می‌شوند.
 


محدودیت‌ها

نیاز به روت:
 اپلیکیشن روی دستگاه‌های غیر روت‌شده کار نمی‌کند.
 
وابستگی به Magisk:
 اسکریپت بوت به دایرکتوری service.d در Magisk وابسته است.
 
نیاز به ری‌استارت دستی:
 تغییرات نیاز به ری‌استارت دستگاه دارند تا اعمال شوند.
 
وابستگی به API: 
تشخیص تایم‌زون به API خارجی وابسته است که در صورت عدم دسترسی به شبکه ممکن است کار نکند.


سلب مسئولیت

این اپلیکیشن نیاز به دسترسی روت دارد و فایل‌های سیستمی را تغییر می‌دهد. استفاده از آن با مسئولیت خودتان است. توسعه‌دهنده هیچ مسئولیتی در قبال آسیب‌های ناشی از استفاده نادرست یا تنظیمات اشتباه ندارد.




TimeZone Configurator is an Android application designed to allow users to set custom time zones for individual apps on a rooted Android device. This is particularly useful for apps that require specific time zones for their functionality, enabling users to override the system time zone on a per-app basis.

Features

App Selection:

Browse and select installed apps to configure their time zones.
 
Custom Time Zones:

 Assign specific time zones to individual apps from a comprehensive list of available time zones.

Search Functionality:

 Quickly search for apps by name or package name.

Configured Apps View: 

View and manage the list of apps with custom time zone settings.

Delete Configurations:

 Remove time zone configurations for specific apps via a context menu.

Boot Persistence:

 Automatically applies time zone settings on device boot using a boot script (requires root).

Network Time Zone Detection:

 Fetches the current time zone from an external API to assist with configuration.

Reboot Prompt:
 Prompts for a device reboot to apply changes, ensuring settings take effect.


Usage

Launch the App:

Open the app on your rooted device. If the device is not rooted, the app will display an error and exit.


Select Apps:

Navigate to the "Select Apps" tab.

Use the search bar to find specific apps.

Choose a time zone for an app from the dropdown menu next to it.



Apply Changes:

Press the "Apply" button in the menu to save the configurations.

A reboot prompt will appear to apply the changes.



View Configured Apps:

Switch to the "Configured Time Zones" tab to see apps with custom time zones.

Long-press an app to delete its configuration.



Boot Script:

On first run, the app installs a boot script (/data/adb/service.d/timezone.sh) to ensure settings persist after a reboot.

The script is executed automatically on device boot (requires Magisk).




Technical Details

Root Access: 

Uses su commands to read/write configuration files (/data/timezone_config.txt) and install the boot script.

Configuration Storage: 

Stores app-specific time zone settings in /data/timezone_config.txt in the format packageName|timeZone.

UI Components:

Uses ViewPager2 with TabLayout for navigating between app selection and configured apps.

Custom ListView adapters (AppAdapter and ConfiguredAppAdapter) for displaying app information and time zone settings.



Limitations

Root Requirement:
 The app will not function on non-rooted devices.
 
Magisk Dependency: 
The boot script relies on Magisk's service.d directory for persistence.

Manual Reboot:
 Changes require a device reboot to take effect.
 



Disclaimer

This app requires root access and modifies system files. Use at your own risk. The developer is not responsible for any damage caused by improper use or misconfiguration.
