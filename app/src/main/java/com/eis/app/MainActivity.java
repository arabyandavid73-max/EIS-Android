package com.eis.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
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
    int blue = Color.rgb(30,58,138);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.rgb(18,18,18));
        getWindow().setNavigationBarColor(Color.rgb(18,18,18));

        android.content.SharedPreferences prefs =
            getSharedPreferences("EIS", MODE_PRIVATE);

        username = prefs.getString("username", "Оператор");
        validCode = prefs.getString("code", "1234");
        dark = prefs.getBoolean("dark", true);
        server = prefs.getString("server", "192.168.1.2");

        showLogin();
    }

    int bg() { return dark ? 0xFF121212 : 0xFFFFFFFF; }
    int panel() { return dark ? 0xFF1E1E1E : 0xFFF3F4F6; }
    int fg() { return dark ? Color.WHITE : 0xFF222222; }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(fg());
        t.setPadding(12,12,12,12);
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams params() {
        return new LinearLayout.LayoutParams(
            -1, -2);
    }

    void showLogin() {
        root = column();
        root.setGravity(Gravity.CENTER);
        root.setPadding(28,28,28,28);
        root.setBackgroundColor(bg());

        TextView title = text("Единая Информационная Система",22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = text("Введите код доступа",14);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        codeInput = new EditText(this);
        codeInput.setHint("Код доступа");
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        codeInput.setTextColor(fg());
        codeInput.setHintTextColor(0xFF888888);
        root.addView(codeInput, params());

        Button login = new Button(this);
        login.setText("ВОЙТИ");
        root.addView(login);

        login.setOnClickListener(v -> {
            if(codeInput.getText().toString().equals(validCode)) {
                logged = true;
                showMain();
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage("Неверный код!")
                    .setPositiveButton("OK",null).show();
            }
        });

        setContentView(root);
    }

    void showMain() {
        root = column();
        root.setBackgroundColor(bg());

        TextView header = text("🌐  Единая Информационная Система",18);
        header.setTextColor(Color.WHITE);
        header.setTypeface(null,Typeface.BOLD);
        header.setBackgroundColor(blue);
        root.addView(header);

        TextView status = text("● Сервер: проверка...",14);
        status.setTextColor(0xFFFF5555);
        status.setBackgroundColor(blue);
        root.addView(status);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));

        sidebar = column();
        sidebar.setBackgroundColor(panel());
        body.addView(sidebar,new LinearLayout.LayoutParams(135,-1));

        content = column();
        content.setPadding(8,8,8,8);
        body.addView(content,new LinearLayout.LayoutParams(0,-1,1));

        addMenu("Сотрудник: " + username, () -> {});
        addMenu("Поиск по базам", () -> showSearch());
        addMenu("Проверка ФНС", () -> showFns());
        addMenu("IP-Инфо", () -> showIp());
        addMenu("Настройки", () -> showSettings());
        addMenu("О программе", () -> showAbout());
        addMenu("Выйти", () -> showLogin());

        setContentView(root);
        showSearch();

        checkServer(status);
    }

    void addMenu(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTextColor(fg());
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(panel()));
        sidebar.addView(b,params());
        b.setOnClickListener(v -> action.run());
    }

    void clear() {
        content.removeAllViews();
    }

    void title(String value) {
        TextView t = text(value,20);
        t.setTypeface(null,Typeface.BOLD);
        content.addView(t);
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(fg());
        e.setHintTextColor(0xFF888888);
        content.addView(e,params());
        return e;
    }

    void showSearch() {
    clear();
    title("Поиск по демонстрационным базам");

    EditText last = field("Фамилия");
    EditText first = field("Имя");
    EditText middle = field("Отчество");
    EditText dob = field("Дата рождения");
    EditText inn = field("ИНН");

    Button search = new Button(this);
    search.setText("Найти");
    content.addView(search);

    TextView result = text("Введите данные для поиска", 14);
    content.addView(result);

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
                try {
                    URL url = new URL(
                        "http://127.0.0.1:5001/search"
                    );

                    HttpURLConnection c =
                        (HttpURLConnection) url.openConnection();

                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setRequestProperty(
                        "Content-Type", "application/json"
                    );

                    c.getOutputStream().write(
                        body.toString().getBytes("UTF-8")
                    );

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(c.getInputStream())
                    );

                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject data =
                        new JSONObject(response.toString());

                    JSONArray results = data.getJSONArray("results");

                    StringBuilder output = new StringBuilder();

                    for (int i = 0; i < results.length(); i++) {
                        output.append(results.getString(i))
                              .append("\n\n");
                    }

                    if (output.length() == 0) {
                        output.append("Ничего не найдено.");
                    }

                    runOnUiThread(() ->
                        result.setText(output.toString())
                    );

                    reader.close();
                    c.disconnect();

                } catch (Exception e) {
                    runOnUiThread(() ->
                        result.setText("Ошибка: " + e.getMessage())
                    );
                }
            }).start();

        } catch (Exception e) {
            result.setText("Ошибка формирования запроса");
        }
    });
}

    void showFns() {
        clear();
        title("Проверка контрагента (ФНС)");
        field("ИНН / ОГРН");

        Button b = new Button(this);
        b.setText("Запросить");
        content.addView(b);
        TextView result = text("Результаты появятся здесь",14);
        content.addView(result);
    }

    void showIp() {
        clear();
        title("Информация по IP / Домену");
        EditText ip = field("IP или домен");

        Button b = new Button(this);
        b.setText("Проверить");
        content.addView(b);
        TextView result = text("Информация появится здесь",14);
        content.addView(result);

        b.setOnClickListener(v ->
            requestIp(ip.getText().toString(),result));
    }

    void showSettings() {
        clear();
        title("Настройки");

        EditText name = field("Имя сотрудника");
        name.setText(username);

        EditText code = field("Новый код доступа");

        Switch theme = new Switch(this);
        theme.setText("Тёмная тема");
        theme.setTextColor(fg());
        theme.setChecked(dark);
        content.addView(theme);

        Button save = new Button(this);
        save.setText("Сохранить");
        content.addView(save);

        save.setOnClickListener(v -> {
            username = name.getText().toString().trim();

            if(code.length() > 0) {
                if(code.length() < 4) {
                    code.setError("Минимум 4 символа");
                    return;
                }
                validCode = code.getText().toString();
            }

            dark = theme.isChecked();

            getSharedPreferences("EIS",MODE_PRIVATE)
                .edit()
                .putString("username",username)
                .putString("code",validCode)
                .putBoolean("dark",dark)
                .putString("server",server)
                .apply();

            showMain();
        });
    }

    void showAbout() {
        clear();
        title("О программе");
        content.addView(text(
            "Единая Информационная Система\n"+
            "Версия 2.0\n\n"+
            "Программа для работы с демонстрационными базами данных.\n\n"+
            "© 2026",15));
    }

    void checkServer(TextView status) {
        new Thread(() -> {
            boolean online = false;
            try {
                URL url = new URL("http://" + server + ":5001/");
                HttpURLConnection c =
                    (HttpURLConnection)url.openConnection();
                c.setConnectTimeout(3000);
                c.setReadTimeout(3000);
                online = c.getResponseCode() < 500;
                c.disconnect();
            } catch(Exception ignored) {}

            final boolean result = online;
            runOnUiThread(() -> {
                status.setText(result ?
                    "● Сервер: Онлайн" : "● Сервер: Офлайн");
                status.setTextColor(result ?
                    0xFF10B981 : 0xFFEF4444);
            });

            new Handler(Looper.getMainLooper()).postDelayed(
                () -> checkServer(status),10000);
        }).start();
    }

    void requestIp(String query,TextView result) {
        result.setText("IP-проверка доступна после подключения разрешённого источника данных.");
    }
}
