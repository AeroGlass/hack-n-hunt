package aero.glass.utils;

import android.content.res.AssetManager;
import android.os.Environment;

import org.glob3.mobile.generated.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tool class that is used to load custom data from JSON files
 * Created by DrakkLord on 2015.06.12..
 */
public final class JSONHelper {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    private static final String LATITUDE_STRING = "Latitude";
    private static final String LONGITUDE_STRING = "Longitude";
    private static final String IDENT_STRING = "Ident";

    private JSONHelper() {
    }

    public static int getNextObjectID() {
        return ATOMIC_INTEGER.addAndGet(1);
    }

    public static JSONModel createJSONModel(final AssetManager a, JSONObject item, String path)
            throws JSONException {
        double scale = 1;
        double rotationPitch = 0;
        double rotationHeading = 0;

        JSONModel oj = new JSONModel(new JSONModel.JSONFileReader() {
            private String prefix = null;

            @Override
            public String read(String fn) {
                if (fn.endsWith(".zip")) {
                    prefix = URL.FILE_PROTOCOL + fn;
                    String json = FileIOHelper.loadStringFromZIP(a, "model.json", fn);
                    if (!json.isEmpty()) {
                        return json;
                    } else {
                        String jsonFilenName = fn.substring(fn.lastIndexOf(File.separatorChar) + 1,
                                fn.lastIndexOf('.')) + ".json";
                        return FileIOHelper.loadStringFromZIP(a, jsonFilenName, fn);
                    }
                } else {
                    prefix = URL.FILE_PROTOCOL + fn.substring(0, fn.lastIndexOf(File.separatorChar))
                            + File.separatorChar;
                    try {
                        return FileIOHelper.loadString(a, fn);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            }

            @Override
            public String getPrefix() {
                return prefix;
            }
        });

        if (!item.isNull("modelFile")) {
            File dir = new File(Environment.getExternalStorageDirectory(), "aeroglass");
            String filePath = path + File.separator + item.getString("modelFile");
            File txtFile = new File(dir, filePath);

            if (txtFile.exists()) {
                oj.setJsonFileName(Environment.getExternalStorageDirectory() + File.separator
                        + "aeroglass" + File.separator + filePath);
            } else {
                oj.setJsonFileName(filePath);
            }
        }
        oj.setIdent(item.getString(IDENT_STRING));
        oj.setLatitude(item.getDouble(LATITUDE_STRING));
        oj.setLongitude(item.getDouble(LONGITUDE_STRING));
        oj.setAltitudeInFeet(item.getDouble("AltitudeInFeet"));

        if (!item.isNull("Scale")) {
            scale *= item.getDouble("Scale");
        }
        if (!item.isNull("RotationPitch")) {
            rotationPitch += item.getDouble("RotationPitch");
        }
        if (!item.isNull("RotationHeading")) {
            rotationHeading += item.getDouble("RotationHeading");
        }

        oj.setScale(scale);
        oj.setRotationPitch(rotationPitch);
        oj.setRotationHeading(rotationHeading);

        if (!item.isNull("RotationHeadingRate")) {
            oj.setRotationRateHeading(item.getDouble("RotationHeadingRate"));
        }

        return oj;
    }

    public static JSONModel getModel(AssetManager a, String fn, String ident) {
        for (JSONModel jsonModel : getJsonModels(a, fn)) {
            if (jsonModel.getIdent().equalsIgnoreCase(ident)) {
                return jsonModel;
            }
        }
        return null;
    }

    public static List<JSONModel> getJsonModels(AssetManager a, String fn) {
        final ArrayList<JSONModel> jobjList = new ArrayList<JSONModel>();
        try {
            final JSONArray ja = new JSONArray(FileIOHelper.loadString(a, fn));
            final String path = fn.substring(0, fn.lastIndexOf(File.separatorChar));

            for (int i = 0; i < ja.length(); i++) {
                jobjList.add(createJSONModel(a, (JSONObject) ja.get(i), path));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobjList;
    }
}
