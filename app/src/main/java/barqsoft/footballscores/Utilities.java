package barqsoft.footballscores;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {

    private static final String LOG_TAG = "Utilities";
    public static final int CHAMPIONS_LEAGUE = 362;

    /**
     * Get name of league by id.
     *
     * @param context Context
     * @param league_num int
     * @return String name of League
     */
    public static String getLeague(Context context, int league_num) {
        String league = context.getString(R.string.league_not_found);
        try {
            league = context.getResources().getString(context.getResources()
                    .getIdentifier(String.format("league_%d", league_num), "string", context.getPackageName()));
        } catch (Exception e) {
            Log.e(LOG_TAG, "getLeague: " + e.getMessage());
        }

        return league;
    }

    public static String getMatchDay(int match_day,int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname) {
        if (teamname==null){
            return R.drawable.football;
        }
        switch (teamname) {
            //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.football;
        }
    }

    public static boolean hasJellyBeanMr1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }
}
