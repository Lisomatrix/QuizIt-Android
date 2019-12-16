package pt.lisomatrix.ptproject.singleton;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import pt.lisomatrix.ptproject.messages.ScoreMessage;
import pt.lisomatrix.ptproject.model.Message;
import pt.lisomatrix.ptproject.model.Question;
import pt.lisomatrix.ptproject.model.Room;
import pt.lisomatrix.ptproject.model.TopScore;
import pt.lisomatrix.ptproject.model.User;
import pt.lisomatrix.ptproject.service.NetworkService;

public class StateSingleton {

    private static StateSingleton instance;

    public static void init(Context context) {
        if (instance == null) {
            instance = new StateSingleton(context);
        }
    }

    public static StateSingleton getInstance() {
        return instance;
    }

    private static final String ME = "ME";
    private static final String SCORE = "SCORE";
    private static final String QUESTION = "QUESTION";
    private static final String JOINED_ROOM = "JOINED_ROOM";


    private Subject<AbstractMap.SimpleEntry<Boolean, User>> userSubject = PublishSubject.create();
    private BehaviorSubject<ScoreMessage> scoreSubject = BehaviorSubject.create();
    private Subject<Boolean> endSubject = PublishSubject.create();
    private BehaviorSubject<Question> questionSubject = BehaviorSubject.create();
    private BehaviorSubject<List<Room>> roomsSubject = BehaviorSubject.create();
    private BehaviorSubject<Room> joinedRoomSubject = BehaviorSubject.create();

    private BehaviorSubject<Integer> joinedRoomDeleted = BehaviorSubject.create();

    private BehaviorSubject<List<TopScore>> topScoreSubject = BehaviorSubject.create();

    private List<Question> questions = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    private BehaviorSubject<User> meSubject = BehaviorSubject.create();

    private BehaviorSubject<Map<String, List<Question>>> wrongQuestionsSubject = BehaviorSubject.create();

    private Disposable messagesDisposable;

    private BehaviorSubject<Boolean> isConnectedSubject = BehaviorSubject.createDefault(false);

    private NetworkService networkService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.LocalBinder) service).getService();
            setMessagesObservable(networkService.getMessages());
            networkService.getConnectObservable().subscribe(isConnectedSubject::onNext);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isConnectedSubject.onNext(false);
        }
    };

    private Context context;

    public StateSingleton(Context context) {
        this.context = context;
        Intent startService = new Intent(context, NetworkService.class);

        context.bindService(startService, mServiceConnection, context.BIND_AUTO_CREATE);
        context.startService(startService);
    }

    public void setIsInBackground(boolean background) {
        if (networkService != null)
        networkService.setIsInBackground(background);
    }

    public Observable<Boolean> getIsConnected() {
        return isConnectedSubject.serialize();
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public void clearJoinedRoom() {
        //joinedRoomSubject = BehaviorSubject.create();
    }

    public Observable<List<TopScore>> getTopScore() {
        return topScoreSubject.serialize();
    }

    public Observable<Integer> isJoinedRoomDeleted() {
        return joinedRoomDeleted.serialize();
    }

    public Observable<Boolean> getStart() {
        return endSubject.serialize();
    }

    public Observable<User> getMe() {
        return meSubject.toSerialized();
    }

    public void setMessagesObservable(Observable<Message> messagesObservable) {
        if (messagesDisposable != null && !messagesDisposable.isDisposed()) {
            messagesDisposable.dispose();
        }

        messagesDisposable = messagesObservable.subscribe(this::handleMessage);
    }

    public Observable<Room> getJoinedRoom() {
        return joinedRoomSubject.serialize();
    }

    public Observable<AbstractMap.SimpleEntry<Boolean, User>> getUsers() {
        return userSubject.serialize();
    }

    public Observable<Question> getQuestions() {
        return questionSubject.toSerialized();
    }

    public Observable<ScoreMessage> getScore() {
        return scoreSubject.toSerialized();
    }

    public Observable<Boolean> hasEnded() {
        return endSubject.toSerialized();
    }

    public Observable<List<Room>> getRooms() {
        return roomsSubject;
    }

    public Observable<Map<String, List<Question>>> getWrongQuestions() {
        return wrongQuestionsSubject.serialize();
    }

    private void handleMessage(Message message) {
        try {

            switch (message.getType()) {
                case USER_CREATED:
                    handleUserCreated(message);
                    break;
                case ROOM_CREATED:
                case ROOM_JOINED:
                    handleRoomCreatedOrJoined(message);
                    break;
                case USER_JOIN:
                    handleUserJoin(message);
                    break;
                case USER_LEFT:
                    handleUserLeft(message);
                    break;
                case NEW_QUESTION:
                    handleNewQuestion(message);
                    break;
                case ANSWER_RESPONSE:
                    break;
                case SCORE:
                    handleScore(message);
                    break;
                case START:
                    endSubject.onNext(false);
                    break;
                case END:
                    handleEnd();
                    break;
                case NEW_ROOM:
                    handleNewRoom(message);
                    break;
                case GET_ROOMS:
                    handleGetRooms(message);
                    break;
                case DELETE_ROOM:
                    handleDeleteRoom(message);
                    break;
                case TOP_SCORE:
                    handleTopScore(message);
                    break;
                case WRONG_QUESTIONS:
                    handleWrongQuestions(message);
                    break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void handleWrongQuestions(Message message) {
        Map<String, List<Question>> wrongQuestions = objectMapper.convertValue(message.getData(), new TypeReference<Map<String, List<Question>>>(){});

        wrongQuestionsSubject.onNext(wrongQuestions);
    }

    private void handleTopScore(Message message) {
        List<TopScore> topScores = objectMapper.convertValue(message.getData(), new TypeReference<List<TopScore>>(){});

        topScoreSubject.onNext(topScores);
    }

    private void handleDeleteRoom(Message message) {
        int roomId = objectMapper.convertValue(message.getData(), Integer.class);
        List<Room> rooms = roomsSubject.getValue();

        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getId() == roomId) {
                rooms.remove(i);
                break;
            }
        }

        if (joinedRoomSubject.getValue().getId() == roomId) {
            joinedRoomDeleted.onNext((int) message.getData());
        }

        roomsSubject.onNext(rooms);
    }

    private void handleUserJoin(Message message) {
        User user = objectMapper.convertValue(message.getData(), User.class);
        userSubject.onNext(new AbstractMap.SimpleEntry<>(true, user));
        Room currentRoom = joinedRoomSubject.getValue();

        if (currentRoom != null)
            currentRoom.getParticipants().add(user);

        joinedRoomSubject.onNext(currentRoom);
    }

    private void handleUserLeft(Message message) {
        User user = objectMapper.convertValue(message.getData(), User.class);
        userSubject.onNext(new AbstractMap.SimpleEntry<>(false, user));
        Room currentRoom = joinedRoomSubject.getValue();

        if (currentRoom != null)
            currentRoom.getParticipants().remove(user.getId());

        joinedRoomSubject.onNext(currentRoom);

    }

    private void handleUserCreated(Message message) {
        meSubject.onNext(objectMapper.convertValue(message.getData(), User.class));
    }

    private void handleRoomCreatedOrJoined(Message message) {
        joinedRoomSubject.onNext((objectMapper.convertValue(message.getData(), Room.class)));
    }

    private void handleNewQuestion(Message message) {
        Question question = objectMapper.convertValue(message.getData(), Question.class);

        questionSubject.onNext(question);
        questions.add(question);
    }

    private void handleScore(Message message) {
        scoreSubject.onNext(objectMapper.convertValue(message.getData(), ScoreMessage.class));
    }

    private void handleEnd() {
        endSubject.onNext(true);
    }

    private void handleGetRooms(Message message) {
        try {
            Room[] newRooms = objectMapper.convertValue(message.getData(), Room[].class);

            List<Room> rooms = new ArrayList<>();
            for (Room room : newRooms) {
                rooms.add(room);
            }

            roomsSubject.onNext(rooms);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void handleNewRoom(Message message) {
        List<Room> rooms = roomsSubject.getValue();

        if (rooms == null) {
            rooms = new ArrayList<>();
        }

        rooms.add(objectMapper.convertValue(message.getData(), Room.class));
        roomsSubject.onNext(rooms);
    }
}
