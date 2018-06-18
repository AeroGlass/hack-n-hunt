package aero.glass.primary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.glob3.mobile.generated.Geodetic3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aero.glass.sensordatacollection.R;
import aero.glass.unit.LengthUnit;

/**
 * Created by zolta on 2018. 03. 22..
 */

public class MenuDialog implements DialogInterface.OnShowListener{
    private AeroActivity activity;
    private AlertDialog routeDialog;

    private LinearLayout baseView;
    private Spinner cnpSpinner;
    private LinearLayout cnpImages;
    private Spinner routeSpinner;
    private Spinner unitSpinner;
    private CheckBox urbanMode;
    private CheckBox lookAhead;

    private ArrayAdapter cnpAdapter;
    private ArrayAdapter routeAdapter;
    private ArrayAdapter unitAdapter;

    private String selectedRouteName = null;
    private int selectedCNP = 0;
    final private Map<String, LengthUnit> unitNameMap = new HashMap<String, LengthUnit>();
    private Map<String, Pair<Geodetic3D, List<Bitmap>>> cnps;
    private List<String> cnpArray;
    final private G3MComponent g3mComponent;

    public MenuDialog(AeroActivity a, G3MComponent g) {
        activity = a;
        g3mComponent = g;

        for (LengthUnit unit : LengthUnit.values()) {
            unitNameMap.put(unit.toString(), unit);
        }

        unitAdapter = new ArrayAdapter(a, android.R.layout.simple_spinner_item, new ArrayList(unitNameMap.keySet()));
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, activity.geoPackageHelper.getRoutes());
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cnpAdapter = getCnpAdapter(activity.activityStateComponent.getLastRoute());
        cnpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    protected void onClickOK() {
        if (selectedRouteName != null && !selectedRouteName.equals(activity.activityStateComponent.getLastRoute())) {
            activity.activityStateComponent.setLastRoute(selectedRouteName);
            g3mComponent.routeSelected(selectedRouteName, cnpArray, cnps);
        }
        g3mComponent.cnpSelected(selectedCNP);
        activity.activityStateComponent.setLastCNP(selectedCNP);
    }

    protected void onClickCancel() {
        activity.finish();
    }

    protected void onClickThird() {
        routeDialog.cancel();
        g3mComponent.setReference();
    }

    @Override
    public void onShow(DialogInterface dialog) {
    }

    protected void show() {
        if (routeDialog == null) {
            createListDialog();
        }
        cnpSpinner.setSelection(activity.activityStateComponent.getLastCNP());

        routeDialog.setOnShowListener(this);
        routeDialog.show();
    }

    protected void createListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        baseView = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.menu, null);
        builder.setView(baseView);

        cnpSpinner = (Spinner) baseView.findViewById(R.id.cnp_spinner);
        cnpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCNP = position;
                cnpImages.removeAllViews();
                Pair<Geodetic3D, List<Bitmap>> bitmaps = cnps.get(cnpAdapter.getItem(position));
                if (bitmaps != null && bitmaps.second != null) {
                    for (Bitmap bitmap : bitmaps.second) {
                        ImageView imageView = new ImageView(activity);
                        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 2, bitmap.getHeight() * 2, true));
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        cnpImages.addView(imageView);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cnpSpinner.setAdapter(cnpAdapter);
        cnpImages = (LinearLayout) baseView.findViewById(R.id.cnp_images);
        routeSpinner = (Spinner) baseView.findViewById(R.id.route_spinner);
        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRouteName = (String) routeAdapter.getItem(position);
                if (!selectedRouteName.equals(activity.activityStateComponent.getLastRoute())) {
                    cnpAdapter = getCnpAdapter(selectedRouteName);
                    cnpSpinner.setSelection(0);
                    cnpSpinner.setAdapter(cnpAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        routeSpinner.setAdapter(routeAdapter);
        String lastRoute = activity.activityStateComponent.getLastRoute();
        if (lastRoute != null) {
            routeSpinner.setSelection(routeAdapter.getPosition(lastRoute));
        }

        unitSpinner = (Spinner) baseView.findViewById(R.id.unit_spinner);
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.activityStateComponent.setLengthUnit(LengthUnit.valueOf((String) unitAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(unitAdapter.getPosition((String) activity.activityStateComponent.getLengthUnit().toString()));

        urbanMode = (CheckBox) baseView.findViewById(R.id.urban_mode);
        urbanMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activity.activityStateComponent.setUrbanMode(isChecked);
                activity.sensorComponent.resetCage();
            }
        });
        urbanMode.setChecked(activity.activityStateComponent.isUrbanMode());

        lookAhead = (CheckBox) baseView.findViewById(R.id.look_ahead);
        lookAhead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activity.activityStateComponent.setLookAhead(isChecked);
            }
        });
        lookAhead.setChecked(activity.activityStateComponent.isLookAhead());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickOK();
            }
        });

        builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickCancel();
            }
        });

        builder.setNeutralButton("NEW REFERENCE", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickThird();
            }
        });

        routeDialog = builder.create();
    }

    private ArrayAdapter getCnpAdapter(String route) {
        cnps = activity.geoPackageHelper.getCoordImageMap(G3MComponent.CNP_FEATURE_NAME, route);
        cnpArray = g3mComponent.sortCNPs(cnps, route);
        ArrayAdapter arrayAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, cnpArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return arrayAdapter;
    }
}
