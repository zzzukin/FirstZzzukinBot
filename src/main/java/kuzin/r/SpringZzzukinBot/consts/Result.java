package kuzin.r.SpringZzzukinBot.consts;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public enum Result {
    EXCELLENT("Превосходно"),
    GOOD("Хорошо"),
    SATISFACTORY("Удовлетворительно"),
    BAD("Плохо"),
    UNSATISFACTORY("Очень плохо");

    final InlineKeyboardButton button;
    final String text;

    Result(String text) {
        this.button = new InlineKeyboardButton();
        this.text = text;
        button.setText(text);
        button.setCallbackData(this.name());
    }

    public InlineKeyboardButton getButton() {
        return button;
    }

    public String getText() {
        return text;
    }
}
