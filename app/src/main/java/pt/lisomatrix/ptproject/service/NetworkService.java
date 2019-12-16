package pt.lisomatrix.ptproject.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import pt.lisomatrix.ptproject.R;
import pt.lisomatrix.ptproject.UI.main.MainActivity;
import pt.lisomatrix.ptproject.constants.MessageType;
import pt.lisomatrix.ptproject.messages.AnswerMessage;
import pt.lisomatrix.ptproject.model.Message;

public class NetworkService extends Service {

    private static final String NAME = "QUIZIT";
    private static final String CHANNEL_ID = "QUIZIT";
    public static final int START_ID = 1;

    private OkHttpClient client = new OkHttpClient();
    private LocalBinder mBinder = new LocalBinder();
    private WebSocket webSocket;
    private ObjectMapper objectMapper = new ObjectMapper();
    private boolean isConnected = false;

    private Subject<Message> messageSubject = PublishSubject.create();
    private Subject<Boolean> connectedSubject = BehaviorSubject.createDefault(isConnected);

    private boolean isInBackground = false;

    @Override
    public IBinder onBind(Intent intent) {
        connect();

        createNotificationChannel();
        return mBinder;
    }

    public Observable<Boolean> getConnectObservable() {
        return connectedSubject.serialize();
    }

    public void sendGetTopScore() {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.TOP_SCORE, null));
    }

    public void sendAnswerRequest(AnswerMessage answerMessage) {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.ANSWER, answerMessage));
    }

    public void sendJoinRoomRequest(String name) {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.JOIN_ROOM, name));
    }

    public void sendJoinRoomRequest(int roomId) {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.JOIN_ROOM, roomId));
    }

    public void sendCreateUserRequest(String username) {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.NEW_USER, username));
    }

    public void sendCreateRoomRequest(String roomName) {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.CREATE_ROOM, roomName));
    }

    public void sendGetRoomsRequest() {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.GET_ROOMS, null));
    }

    public void sendStartRequest() {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.START, null));
    }

    public void sendDeleteRoom() {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.DELETE_ROOM, null));
    }

    public void sendLeaveRoom() {
        if (!isConnected)
            return;

        webSocket.send(createMessage(MessageType.LEAVE_ROOM, null));
    }

    public Observable<Message> getMessages() {
        return messageSubject.serialize();
    }

    private String createMessage(MessageType type, Object object) {
        try {
            return objectMapper.writeValueAsString(new Message(type, object));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void setIsInBackground(boolean background) {
        isInBackground = background;
    }

    private void connect() {

        if (webSocket == null || !isConnected) {
            Log.d("INFO", "Attempting to connect !");

            Request request = new Request.Builder().url("wss://pt-quiz-it.herokuapp.com/websocket/chat").build();
            //Request request = new Request.Builder().url("wss://192.168.1.5:8080/websocket/chat").build();
            //Request request = new Request.Builder().url("ws://10.101.254.16:8080/websocket/chat").build();
            //Request request = new Request.Builder().url("ws://10.0.2.2:8080/websocket/chat").build();

            CustomWebSocketListener listener = new CustomWebSocketListener();
            webSocket = client.newWebSocket(request, listener);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "QUIZIT NOTIFICATIONS";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class CustomWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            Log.d("INFO", "Connected to the server !");
            super.onOpen(webSocket, response);
            isConnected = true;
            connectedSubject.onNext(isConnected);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            Log.d("RECEIVED MESSAGE:", text);

            if (text.equals("PING")) {
                Log.d("PING", "PONG SENT");
                webSocket.send("PONG");
                return;
            }

            try {
                Message message = objectMapper.readValue(text, Message.class);

                messageSubject.onNext(message);

                JSONObject jsonObject = new JSONObject(text);


                MessageType type = MessageType.valueOf(jsonObject.getString("type"));

                if (type == MessageType.USER_CREATED) {
                    sendGetRoomsRequest();
                } else if (type == MessageType.START) {
                    createNotificationChannel();

                    if (isInBackground) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("NOTIFICATION", "START");

                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.logo)
                                .setChannelId(CHANNEL_ID)
                                .setContentTitle("Room started!")
                                .setContentText("Your room has just started, good luck!")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                // Set the intent that will fire when the user taps the notification
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(START_ID, builder.build());
                    }
                }

            } catch (Exception ex) { }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            Log.d("INFO", "Disconnected from the server !");
            super.onClosing(webSocket, code, reason);
            if (isConnected != false) {
                isConnected = false;
                connectedSubject.onNext(isConnected);
            }
            Observable.timer(5, TimeUnit.SECONDS).subscribe((time) -> connect());
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Log.d("INFO", "Attempt to connect to the server failed");
            if (isConnected != false) {
                isConnected = false;
                connectedSubject.onNext(isConnected);
            }
            Observable.timer(5, TimeUnit.SECONDS).subscribe((time) -> connect());
        }
    }

    public class LocalBinder extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }

        public void setCallbacks() {
            // TODO: Implement interface and update this
        }
    }
}
