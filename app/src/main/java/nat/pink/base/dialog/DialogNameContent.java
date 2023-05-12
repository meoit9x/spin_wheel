package nat.pink.base.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;

import nat.pink.base.R;
import nat.pink.base.databinding.DialogNameContentBinding;
import nat.pink.base.model.ObjectsContentSpin;
import nat.pink.base.utils.Utils;

public class DialogNameContent extends Dialog {

    private Consumer<String> consumer;
    private DialogNameContentBinding binding;
    private List<ObjectsContentSpin> objectDefaults = new ArrayList<>();
    private Activity context;
    private String string = "";
    private Drawable drawable;
    private boolean checkSameText;

    public DialogNameContent(@NonNull Activity context, int themeResId, Drawable drawable, String string, Consumer<String> consumer) {
        super(context, themeResId);
        this.consumer = consumer;
        this.context = context;
        this.string = string;
        this.drawable = drawable;
    }

    public DialogNameContent(@NonNull Activity context, int themeResId, Drawable drawable, String string, List<ObjectsContentSpin> objectDefaults, Consumer<String> consumer) {
        super(context, themeResId);
        this.consumer = consumer;
        this.context = context;
        this.string = string;
        this.drawable = drawable;
        this.objectDefaults = objectDefaults;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = DialogNameContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.llRoot.setBackground(drawable);
        binding.txtCancel.setOnClickListener(view -> {
            Utils.hiddenKeyboard(context, binding.txtContent);
            dismiss();
        });
        binding.txtOk.setOnClickListener(view -> {
            for (ObjectsContentSpin objectsContentSpin : objectDefaults) {
                if (objectsContentSpin.getContent().equalsIgnoreCase(binding.txtContent.getText().toString())) {
                    checkSameText = true;
                    break;
                } else {
                    checkSameText = false;
                }
            }
            if (binding.txtContent.getText().length() == 0 || checkSameText) {
                binding.txtError.setVisibility(View.VISIBLE);
                if(checkSameText){
                    binding.txtError.setText(R.string.error_same_name);
                }
                return;
            }
            if (binding.txtError.getVisibility() == View.VISIBLE)
                return;
            consumer.accept(binding.txtContent.getText().toString().trim());
            Utils.hiddenKeyboard(context, binding.txtContent);
            dismiss();
        });
        binding.txtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.txtError.setText(context.getString("".equals(s.toString()) ? R.string.error_blank_content : R.string.error_character));
                binding.txtError.setVisibility("".equals(s.toString()) || s.toString().length() > 14 ? View.VISIBLE : View.GONE);
            }
        });
        binding.txtContent.setText(string);
    }
}
