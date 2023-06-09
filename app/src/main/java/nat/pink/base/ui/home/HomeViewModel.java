package nat.pink.base.ui.home;

import android.content.Context;

import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import nat.pink.base.base.BaseViewModel;
import nat.pink.base.dao.DatabaseController;
import nat.pink.base.model.ObjectLocation;
import nat.pink.base.model.ObjectSpin;
import nat.pink.base.model.ObjectSpinDisplay;
import nat.pink.base.model.ObjectsContentSpin;
import nat.pink.base.network.RequestAPI;
import nat.pink.base.utils.Utils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends BaseViewModel {

    public MutableLiveData<ObjectSpin> reloadData = new MutableLiveData<>();
    public MutableLiveData<List<ObjectsContentSpin>> reloadDataContent = new MutableLiveData<>();
    public MutableLiveData<HashMap<String, List<ObjectSpinDisplay>>> reloadDataListSpin = new MutableLiveData<>();
    private static String content = "";
    public static final String KEY_SUGGEST = "KEY_SUGGEST";
    public static final String KEY_YOUR_SPIN = "KEY_YOUR_SPIN";

    public void getDefaultSpin(Context context) {
        ObjectSpin objectSpin =DatabaseController.getInstance(context).getDefaultSpin();
        reloadData.postValue(objectSpin);
    }

    public void getContentSpinById(Context context, long id) {
        reloadDataContent.postValue(DatabaseController.getInstance(context).getContentSpinByTime(id));
    }

    public void getSpinAndContent(Context context) {
        List<ObjectSpin> objectSpins = DatabaseController.getInstance(context).getAllSpin();

        List<ObjectSpinDisplay> objectSpinDisplays = new ArrayList<>();
        List<ObjectSpinDisplay> yourObjectSpinDisplays = new ArrayList<>();

        List<ObjectsContentSpin> mListContentSpins = DatabaseController.getInstance(context).getAllContentSpin();
        HashMap<String, List<ObjectSpinDisplay>> stringListHashMap = new HashMap<>();
        objectSpins.forEach(objectSpin -> {
            ObjectSpinDisplay objectSpinDisplay = new ObjectSpinDisplay();
            objectSpinDisplay.setObjectSpin(objectSpin);
            List<ObjectsContentSpin> objectsContentSpins = mListContentSpins.stream().filter(objectsContentSpin -> objectsContentSpin.getDate() == objectSpin.getDate()).collect(Collectors.toList());
            content = "";
            objectsContentSpins.forEach(objectsContentSpin -> {
                content += objectsContentSpin.getContent() + ", ";
            });
            objectSpinDisplay.setContent(content);
            if (objectSpin.isSuggest())
                objectSpinDisplays.add(objectSpinDisplay);
            else
                yourObjectSpinDisplays.add(objectSpinDisplay);
        });
        stringListHashMap.put(KEY_SUGGEST, objectSpinDisplays);
        stringListHashMap.put(KEY_YOUR_SPIN, yourObjectSpinDisplays);
        reloadDataListSpin.postValue(stringListHashMap);
    }


    public void updateIsDefault(Context context, long date) {
        DatabaseController.getInstance(context).updateIsDefault(date);
        getSpinAndContent(context);
        getDefaultSpin(context);
    }

    public void updateSpin(Context context, ObjectSpin objectSpin, List<ObjectsContentSpin> objectsContentSpins) {
        DatabaseController.getInstance(context).updateContentSpins(objectSpin, objectsContentSpins);
        getContentSpinById(context, objectSpin.getDate());
        getSpinAndContent(context);
        getDefaultSpin(context);
    }

    public void insertSpin(Context context, ObjectSpin objectSpin, List<ObjectsContentSpin> objectsContentSpins) {
        DatabaseController.getInstance(context).updateContentSpins(objectSpin, objectsContentSpins);
        getSpinAndContent(context);
    }

    public void deleteContentSpin(Context context, ObjectSpin objectSpin){
        DatabaseController.getInstance(context).deleteContentSpins(objectSpin);
        getSpinAndContent(context);
    }

    public void checkLocation(RequestAPI requestAPI, Context context, String phone, String content, Consumer consumer) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", phone);
            jsonObject.put("package", Utils.getPackageName(context));
            jsonObject.put("content", content);
            jsonObject.put("simulator", Utils.isEmulator(context) ? "true" : "false");
            jsonObject.put("appName", Utils.getAppName(context));
            RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            requestAPI.checkLocation(requestBody).enqueue(new Callback<ObjectLocation>() {
                @Override
                public void onResponse(Call<ObjectLocation> list, Response<ObjectLocation> response) {
                    if (response.errorBody() != null) {
                        consumer.accept(response.errorBody().toString());
                        return;
                    }
                    consumer.accept(response.body());
                }

                @Override
                public void onFailure(Call<ObjectLocation> list, Throwable t) {
                    consumer.accept(t);
                }
            });

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
