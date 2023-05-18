package com.nikol.sketchit.game;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.nikol.sketchit.ChatLayoutAdapter;
import com.nikol.sketchit.GameMenuActivity;
import com.nikol.sketchit.MainActivity;
import com.nikol.sketchit.databinding.ActivityMainBinding;

import java.util.List;

public class GameLayoutBridge {
    private final ActivityMainBinding binding;
    private final MainActivity mainActivity;
    private final ChatLayoutAdapter chatLayoutAdapter;
    private final ChatLayoutManager chatLayoutManager;


    public GameLayoutBridge(ActivityMainBinding binding, MainActivity mainActivity) {
        this.binding = binding;
        this.mainActivity = mainActivity;

        chatLayoutAdapter = new ChatLayoutAdapter();
        chatLayoutManager = new ChatLayoutManager(mainActivity);
        binding.layoutChat.setLayoutManager(chatLayoutManager);
        binding.layoutChat.setAdapter(chatLayoutAdapter);
        binding.layoutChat.setPaintView(binding.paintView);

        chatLayoutManager.setScrollEnabled(true);
        chatLayoutManager.scrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setLoadState() {
        binding.progressBarLayout.setVisibility(View.VISIBLE);
        binding.layoutTools.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.imageCanvas.setVisibility(View.INVISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
    }

    public void setPaintState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.VISIBLE);
        binding.layoutPaintColors.setVisibility(View.VISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.inputVariant.setVisibility(View.INVISIBLE);
        binding.imageCanvas.setVisibility(View.INVISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(true);
    }

    public void setWatchState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.VISIBLE);
        binding.inputVariant.setVisibility(View.VISIBLE);
        binding.imageCanvas.setVisibility(View.VISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
    }

    public void updateChat(List<String> message) {
        chatLayoutManager.setScrollEnabled(true);
        chatLayoutAdapter.getUpdates(message);
        chatLayoutManager.scrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setEnterButtonOnClickListener(View.OnClickListener listener) {
        binding.buttonEnterVariant.setOnClickListener(listener);
    }

    public boolean isVariantEmpty() {
        return binding.inputVariant.getText().toString().isEmpty();
    }

    public String getVariant() {
        return binding.inputVariant.getText().toString();
    }

    public Bitmap getCanvasImage() {
        return binding.paintView.getCanvas();
    }

    public void endGame() {
        Intent intent = new Intent(mainActivity, GameMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainActivity.startActivity(intent);
        mainActivity.finish();
    }

    public void updateRecycleViewPosition() {
        chatLayoutManager.setScrollEnabled(true);
        binding.layoutChat.smoothScrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setCanvas(byte[] canvas) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(canvas, 0, canvas.length);
        binding.imageCanvas.setImageBitmap(bitmap);
    }

    public Context getContext() {
        return mainActivity;
    }

    public void setWord(String word) {
        binding.textWord.setText(word);
    }

    static class ChatLayoutManager extends androidx.recyclerview.widget.LinearLayoutManager {
        private boolean scrolledEnabled;

        public ChatLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean enabled) {
            scrolledEnabled = enabled;
        }

        @Override
        public boolean canScrollVertically() {
            return scrolledEnabled;
        }
    }
}
