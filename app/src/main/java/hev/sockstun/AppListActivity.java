package hev.sockstun;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class AppListActivity extends ListActivity {
    private boolean isChanged = false;
    private Preferences prefs;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Package {
        public PackageInfo info;
        public String label;
        public boolean selected;

        public Package(PackageInfo info, boolean selected, String label) {
            this.info = info;
            this.selected = selected;
            this.label = label;
        }
    }

    @SuppressLint("WrongConstant")
    private class AppArrayAdapter extends ArrayAdapter<Package> {
        public AppArrayAdapter(Context context) {
            super(context, R.layout.appitem);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
            View rowView = inflater.inflate(R.layout.appitem, parent, false);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            TextView textView = (TextView) rowView.findViewById(R.id.name);
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checked);
            Package pkg = getItem(position);
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo appinfo = pkg.info.applicationInfo;
            imageView.setImageDrawable(appinfo.loadIcon(pm));
            textView.setText(appinfo.loadLabel(pm).toString());
            checkBox.setChecked(pkg.selected);
            return rowView;
        }
    }

    @SuppressLint("WrongConstant")
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setChoiceMode(2);
        this.prefs = new Preferences(this);
        Set<String> apps = this.prefs.getApps();
        PackageManager pm = getPackageManager();
        AppArrayAdapter adapter = new AppArrayAdapter(this);
        for (PackageInfo info : pm.getInstalledPackages(4096)) {
            if(this.prefs.getxitong()){
                if(!isSystemApp(info)){
                    if (!info.packageName.equals(getPackageName()) && info.requestedPermissions != null && Arrays.asList(info.requestedPermissions).contains("android.permission.INTERNET")) {
                        boolean selected = apps.contains(info.packageName);
                        String label = info.applicationInfo.loadLabel(pm).toString();
                        Package pkg = new Package(info, selected, label);
                        adapter.add(pkg);
                    }
                }
            }else {
                if (!info.packageName.equals(getPackageName()) && info.requestedPermissions != null && Arrays.asList(info.requestedPermissions).contains("android.permission.INTERNET")) {
                    boolean selected = apps.contains(info.packageName);
                    String label = info.applicationInfo.loadLabel(pm).toString();
                    Package pkg = new Package(info, selected, label);
                    adapter.add(pkg);
                }
            }
        }

        adapter.sort(new Comparator<Package>() { // from class: hev.sockstun.AppListActivity.1
            public int compare(Package a, Package b) {
                if (a.selected != b.selected) {
                    return a.selected ? -1 : 1;
                }
                return a.label.compareTo(b.label);
            }
        });
        setListAdapter(adapter);
    }

    @Override // android.app.ListActivity, android.app.Activity
    protected void onDestroy() {
        if (this.isChanged) {
            AppArrayAdapter adapter = (AppArrayAdapter) getListView().getAdapter();
            Set<String> apps = new HashSet<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                Package pkg = adapter.getItem(i);
                if (pkg.selected) {
                    apps.add(pkg.info.packageName);
                }
            }
            this.prefs.setApps(apps);
        }
        super.onDestroy();
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AppArrayAdapter adapter = (AppArrayAdapter) l.getAdapter();
        adapter.getItem(position).selected = !adapter.getItem(position).selected;
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.checked);
        checkbox.setChecked(adapter.getItem(position).selected);
        this.isChanged = true;
    }

    private boolean isSystemApp(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
