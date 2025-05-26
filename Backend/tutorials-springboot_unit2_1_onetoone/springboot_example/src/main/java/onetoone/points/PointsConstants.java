package onetoone.points;

/**
 * Constants for point values in the system
 */
public class PointsConstants {
    // Action names
    public static final String ACTION_DAILY_LOGIN = "daily_login";
    public static final String ACTION_WORKOUT_COMPLETE = "workout_complete";
    public static final String ACTION_CREATE_TEMPLATE = "create_template";
    public static final String ACTION_JOIN_GROUP = "join_group";
    public static final String ACTION_ADD_FRIEND = "add_friend";
    public static final String ACTION_STREAK = "workout_streak";

    // Point values
    public static final int POINTS_DAILY_LOGIN = 10;
    public static final int POINTS_COMPLETED_WORKOUT = 40;
    public static final int POINTS_CREATED_WORKOUT_TEMPLATE = 40;
    public static final int POINTS_JOINED_GROUP = 20;
    public static final int POINTS_ADDED_FRIEND = 10;

    // Streak rewards
    public static final int POINTS_3_DAY_STREAK = 15;
    public static final int POINTS_7_DAY_STREAK = 30;
    public static final int POINTS_30_DAY_STREAK = 100;
}
