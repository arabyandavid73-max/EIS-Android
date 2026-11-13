
package com.eis.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.text.InputType;
import org.json.*;
import java.net.*;
import java.io.*;

public class MainActivity extends Activity {

    LinearLayout root, content, sidebar;
    EditText codeInput;

    boolean dark = true;
    boolean logged = false;

    String username = "Оператор";
    String validCode = "1234";
    String server = "192.168.1.2";

    final int BG = 0xFF101216;
    final int PANEL = 0xFF1A1E24;
    final int CARD = 0xFF222831;
    final int ACCENT = 0xFF00D68F;
    final int MUTED = 0xFF929BA8;

    int bg() {
        return dark ? BG : 0xFFF2F4F7;
    }

    int panel() {
        return dark ? PANEL : 0xFFFFFFFF;
    }

    int card() {
        return dark ? CARD : 0xFFE8EDF2;
    }

    int fg() {
        return dark ? Color.WHITE : 0xFF202630;
    }

    GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(fg());
        t.setPadding(12, 10, 12, 10);
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams params() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    void styleButton(Button b) {
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setPadding(16, 12, 16, 12);
        b.setBackground(rounded(ACCENT, 18));
        b.setElevation(5);

        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f)
                    .setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                       event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f)
                    .setDuration(150).start();
            }
            return false;
        });
    }

    Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        styleButton(b);
        return b;
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(fg());
        e.setHintTextColor(MUTED);
        e.setTextSize(14);
        e.setSingleLine(true);
        e.setPadding(16, 14, 16, 14);
        e.setBackground(rounded(card(), 16));

        LinearLayout.LayoutParams p = params();
        p.setMargins(0, 5, 0, 10);
        content.addView(e, p);

        return e;
    }

    void animateIn(View view) {
        view.setAlpha(0f);
        view.setTranslationY(15f);
        view.animate().alpha(1f).translationY(0f)
            .setDuration(250).start();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        android.content.SharedPreferences prefs =
            getSharedPreferences("EIS", MODE_PRIVATE);

        username = prefs.getString("username", "Оператор");
        validCode = prefs.getString("code", "1234");
        dark = prefs.getBoolean("dark", true);
        server = prefs.getString("server", "192.168.1.2");

        showLogin();
    }

    void showLogin() {
        root = column();
        root.setGravity(Gravity.CENTER);
        root.setPadding(26, 26, 26, 26);
        root.setBackgroundColor(bg());

        LinearLayout box = column();
        box.setPadding(22, 25, 22, 25);
        box.setBackground(rounded(panel(), 24));
        box.setElevation(12);

        TextView logo = text("◈", 46);
        logo.setTextColor(ACCENT);
        logo.setGravity(Gravity.CENTER);
        box.addView(logo);

        TextView title = text("ЕДИНАЯ\nИНФОРМАЦИОННАЯ\nСИСТЕМА", 21);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        box.addView(title);

        TextView subtitle = text("ПАНЕЛЬ АВТОРИЗАЦИИ", 11);
        subtitle.setTextColor(MUTED);
        subtitle.setGravity(Gravity.CENTER);
        box.addView(subtitle);

        codeInput = new EditText(this);
        codeInput.setHint("Код доступа");
        codeInput.setInputType(
            InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );
        codeInput.setTextColor(fg());
        codeInput.setHintTextColor(MUTED);
        codeInput.setPadding(16, 14, 16, 14);
        codeInput.setBackground(rounded(card(), 16));

        LinearLayout.LayoutParams fp = params();
        fp.setMargins(0, 18, 0, 14);
        box.addView(codeInput, fp);

        Button login = button("ВОЙТИ В СИСТЕМУ");
        box.addView(login, params());

        login.setOnClickListener(v -> {
            if (codeInput.getText().toString().equals(validCode)) {
                logged = true;
                showMain();
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Ошибка авторизации")
                    .setMessage("Неверный код доступа")
                    .setPositiveButton("Понятно", null)
                    .show();
            }
        });

        root.addView(box, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
        animateIn(box);
    }

    void showMain() {
        root = column();
        root.setBackgroundColor(bg());

        LinearLayout header = column();
        header.setPadding(15, 13, 15, 13);
        header.setBackground(rounded(panel(), 18));
        header.setElevation(5);

        TextView heading = text("◈  EIS / SYSTEM", 19);
        heading.setTextColor(ACCENT);
        heading.setTypeface(null, Typeface.BOLD);
        header.addView(heading);

        TextView status = text("● Проверка подключения...", 12);
        status.setTextColor(MUTED);
        header.addView(status);

        LinearLayout.LayoutParams hp = params();
        hp.setMargins(10, 10, 10, 7);
        root.addView(header, hp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);

        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));

        sidebar = column();
        sidebar.setPadding(5, 8, 5, 8);
        sidebar.setBackgroundColor(panel());

        body.addView(sidebar,
            new LinearLayout.LayoutParams(112, -1));

        ScrollView scroll = new ScrollView(this);
        content = column();
        content.setPadding(12, 12, 12, 20);
        scroll.addView(content);

        body.addView(scroll,
            new LinearLayout.LayoutParams(0, -1, 1));

        addMenu("ОПЕРАТОР", () -> {});
        addMenu("ПОИСК", () -> showSearch());
        addMenu("ФНС", () -> showFns());
        addMenu("IP INFO", () -> showIp());
        addMenu("НАСТРОЙКИ", () -> showSettings());
        addMenu("О ПРОГРАММЕ", () -> showAbout());
        addMenu("ВЫЙТИ", () -> showLogin());

        setContentView(root);

        showSearch();
        checkServer(status);
    }

    void addMenu(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setTextColor(fg());
        b.setPadding(3, 12, 3, 12);
        b.setBackground(rounded(card(), 12));
        b.setElevation(2);

        LinearLayout.LayoutParams p = params();
        p.setMargins(3, 4, 3, 4);
        sidebar.addView(b, p);

        b.setOnClickListener(v -> {
            v.animate().scaleX(0.96f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).setDuration(100).start();
                    action.run();
                }).start();
        });
    }

    void clear() {
        content.removeAllViews();
    }

    void title(String value) {
        TextView t = text(value, 21);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(fg());
        content.addView(t);
    }

    void showSearch() {
        clear();

        title("Поиск");
        content.addView(text(
            "Демонстрационный поиск по тестовым записям", 12
        ));

        EditText last = field("Фамилия");
        EditText first = field("Имя");
        EditText middle = field("Отчество");
        EditText dob = field("Дата рождения");
        EditText inn = field("ИНН");

        Button search = button("⌕   НАЙТИ");
        content.addView(search);

        TextView result = text(
            "Результаты появятся здесь", 14
        );
        result.setPadding(15, 18, 15, 18);
        result.setBackground(rounded(card(), 18));
        result.setElevation(3);

        LinearLayout.LayoutParams rp = params();
        rp.setMargins(0, 15, 0, 0);
        content.addView(result, rp);

        animateIn(result);

        search.setOnClickListener(v -> {
            try {
                JSONObject fields = new JSONObject();
                fields.put("last", last.getText().toString());
                fields.put("first", first.getText().toString());
                fields.put("middle", middle.getText().toString());
                fields.put("dob", dob.getText().toString());
                fields.put("inn", inn.getText().toString());

                JSONObject body = new JSONObject();
                body.put("fields", fields);

                result.setText("Выполняется поиск...");

                new Thread(() -> {
                    HttpURLConnection c = null;

                    try {
                        URL url = new URL(
                            "http://" + server + ":5001/search"
                        );

                        c = (HttpURLConnection) url.openConnection();
                        c.setRequestMethod("POST");
                        c.setConnectTimeout(5000);
                        c.setReadTimeout(10000);
                        c.setDoOutput(true);
                        c.setRequestProperty(
                            "Content-Type", "application/json; charset=UTF-8"
                        );

                        try (OutputStream os = c.getOutputStream()) {
                            os.write(body.toString().getBytes("UTF-8"));
                        }

                        int code = c.getResponseCode();

                        InputStream stream = code >= 400
                            ? c.getErrorStream() : c.getInputStream();

                        if (stream == null) {
                            throw new IOException("HTTP " + code);
                        }

                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream, "UTF-8")
                        );

                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        reader.close();

                        JSONObject data =
                            new JSONObject(response.toString());

                        JSONArray results =
                            data.optJSONArray("results");

                        StringBuilder output = new StringBuilder();

                        if (results != null) {
                            for (int i = 0; i < results.length(); i++) {
                                output.append(results.optString(i))
                                    .append("\n\n");
                            }
                        }

                        if (output.length() == 0) {
                            output.append("Ничего не найдено.");
                        }

                        String finalOutput = output.toString();

                        runOnUiThread(() ->
                            result.setText(finalOutput)
                        );

                    } catch (Exception e) {
                        runOnUiThread(() ->
                            result.setText(
                                "Ошибка подключения: " + e.getMessage()
                            )
                        );
                    } finally {
                        if (c != null) c.disconnect();
                    }
                }).start();

            } catch (Exception e) {
                result.setText("Ошибка формирования запроса");
            }
        });
    }

    void showFns() {
        clear();
        title("ФНС");
        content.addView(text(
            "Проверка открытых сведений о компаниях", 13
        ));

        EditText query = field("ИНН / ОГРН компании");

        Button b = button("ПРОВЕРИТЬ");
        content.addView(b);

        TextView result = text(
            "Здесь появится результат проверки", 14
        );
        result.setBackground(rounded(card(), 18));
        content.addView(result);

        b.setOnClickListener(v ->
            result.setText(
                "Подключение к источнику ФНС можно добавить отдельно."
            )
        );
    }

    void showIp() {
        clear();
        title("IP / DOMAIN");

        content.addView(text(
            "Проверка технической информации об IP или домене", 13
        ));

        EditText ip = field("IP-адрес или домен");

        Button b = button("ПРОВЕРИТЬ");
        content.addView(b);

        TextView result = text(
            "Информация появится здесь", 14
        );
        result.setBackground(rounded(card(), 18));
        content.addView(result);

        b.setOnClickListener(v ->
            requestIp(ip.getText().toString(), result)
        );
    }

    void showSettings() {
        clear();
        title("Настройки");

        EditText name = field("Имя оператора");
        name.setText(username);

        EditText code = field("Новый код доступа");
        code.setInputType(
            InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        Switch theme = new Switch(this);
        theme.setText("Тёмная тема");
        theme.setTextColor(fg());
        theme.setChecked(dark);
        content.addView(theme);

        Button save = button("СОХРАНИТЬ");
        content.addView(save);

        save.setOnClickListener(v -> {
            username = name.getText().toString().trim();

            if (code.length() > 0) {
                if (code.length() < 4) {
                    code.setError("Минимум 4 символа");
                    return;
                }
                validCode = code.getText().toString();
            }

            dark = theme.isChecked();

            getSharedPreferences("EIS", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .putString("code", validCode)
                .putBoolean("dark", dark)
                .putString("server", server)
                .apply();

            showMain();
        });
    }

    void showAbout() {
        clear();
        title("О программе");

        LinearLayout box = column();
        box.setPadding(18, 18, 18, 18);
        box.setBackground(rounded(card(), 20));
        box.setElevation(4);

        TextView logo = text("◈ EIS", 26);
        logo.setTextColor(ACCENT);
        logo.setTypeface(null, Typeface.BOLD);
        box.addView(logo);

        box.addView(text(
            "Единая Информационная Система\n\n" +
            "Версия 2.0\n\n" +
            "Интерфейс для работы с демонстрационными " +
            "данными и открытыми сведениями.\n\n" +
            "© 2026", 14
        ));

        content.addView(box);
    }

    void checkServer(TextView status) {
        new Thread(() -> {
            boolean online = false;

            try {
                URL url = new URL(
                    "http://" + server + ":5001/"
                );

                HttpURLConnection c =
                    (HttpURLConnection) url.openConnection();

                c.setConnectTimeout(3000);
                c.setReadTimeout(3000);

                online = c.getResponseCode() < 500;
                c.disconnect();

            } catch (Exception ignored) {}

            final boolean result = online;

            runOnUiThread(() -> {
                status.setText(result
                    ? "● SERVER ONLINE"
                    : "● SERVER OFFLINE");

                status.setTextColor(result
                    ? ACCENT
                    : 0xFFFF5555);
            });

            new Handler(Looper.getMainLooper()).postDelayed(
                () -> checkServer(status), 10000
            );

        }).start();
    }

    void requestIp(String query, TextView result) {
        if (query.trim().isEmpty()) {
            result.setText("Введите IP или домен.");
            return;
        }

        result.setText(
            "Проверка технической информации доступна " +
            "после подключения разрешённого источника данных."
        );
    }
}

