package nat.pink.base.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import nat.pink.base.R;
import nat.pink.base.databinding.DialogConfirmBinding;

public class DialogConfirm extends Dialog {

    private final Consumer<TYPE_DIALOG> consumer;
    private DialogConfirmBinding binding;
    private final Activity context;
    private final TYPE_DIALOG typeDialog;
    private Drawable drawable;

    public enum TYPE_DIALOG {
        TYPE_DELETE, TYPE_ERROR_MIN_CONTENT, TYPE_ERROR_MAX_CONTENT, TYPE_ERROR_MAX_CONTENT_IN_APP, TYPE_ERROR_EMPTY_QUESTION
    }

    public DialogConfirm(@NonNull Activity context, int themeResId, Drawable drawable, TYPE_DIALOG typeDialog, Consumer<TYPE_DIALOG> consumer) {
        super(context, themeResId);
        this.consumer = consumer;
        this.context = context;
        this.typeDialog = typeDialog;
        this.drawable = drawable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = DialogConfirmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.llRoot.setBackground(drawable);

        if (typeDialog == TYPE_DIALOG.TYPE_DELETE) {
            binding.txtContent.setText(context.getString(R.string.delete_this_content));
            binding.txtCancel.setText(context.getString(R.string.cancel_dialog));
            binding.txtOk.setText(context.getString(R.string.delete));
        }
        if (typeDialog == TYPE_DIALOG.TYPE_ERROR_MIN_CONTENT) {
            binding.txtContent.setText(context.getString(R.string.error_min_content));
            binding.txtOk.setText(context.getString(R.string.ok));
            binding.txtCancel.setVisibility(View.GONE);
            binding.llLine.setVisibility(View.GONE);
        }
        if (typeDialog == TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT) {
            binding.txtContent.setText(context.getString(R.string.error_max_content));
            binding.txtOk.setText(context.getString(R.string.ok));
            binding.txtCancel.setText(context.getString(R.string.update));
        }
        if (typeDialog == TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT_IN_APP) {
            binding.txtContent.setText(context.getString(R.string.error_max_content_in_app));
            binding.txtOk.setText(context.getString(R.string.ok));
            binding.txtCancel.setVisibility(View.GONE);
            binding.llLine.setVisibility(View.GONE);
        }
        if (typeDialog == TYPE_DIALOG.TYPE_ERROR_EMPTY_QUESTION) {
            binding.txtContent.setText(context.getString(R.string.error_blank_question));
            binding.txtOk.setText(context.getString(R.string.ok));
            binding.txtCancel.setVisibility(View.GONE);
            binding.llLine.setVisibility(View.GONE);
        }

        binding.txtCancel.setOnClickListener(v -> {
            if (typeDialog == TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT_IN_APP)
                consumer.accept(typeDialog);
            dismiss();
        });
        binding.txtOk.setOnClickListener(v -> {
            if (typeDialog == TYPE_DIALOG.TYPE_DELETE || typeDialog == TYPE_DIALOG.TYPE_ERROR_MIN_CONTENT || typeDialog == TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT)
                consumer.accept(typeDialog);
            dismiss();
        });

        Paint paint = binding.txtOk.getPaint();
        float width = paint.measureText(binding.txtOk.getText().toString());

        Shader textShader = new LinearGradient(0f, 0f, width, binding.txtOk.getTextSize(), new int[]{
                Color.parseColor("#FB77A0"),
                Color.parseColor("#E86B90")
        }, null, Shader.TileMode.REPEAT);
        paint.setShader(textShader);
        binding.txtOk.getPaint().set(paint);
    }
}
