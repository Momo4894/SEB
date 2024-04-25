package seb.model;

import java.util.*;

public class Tips {

    List<Map> tips;
    Map<Integer, String> tipsHeader;
    Map<Integer, String> tipsDetails;

    public Tips() {
        tipsHeader = new HashMap<>();
        tipsDetails = new HashMap<>();

        tipsHeader.put(1, "Keep Your Body Straight");
        tipsHeader.put(2, "Hand Placement");
        tipsHeader.put(3, "Elbow Angle");
        tipsHeader.put(4, "Controlled Movements");
        tipsHeader.put(5, "Full Range of Motion");
        tipsHeader.put(6, "Breathing");
        tipsHeader.put(7, "Focus on Form");
        tipsHeader.put(8, "Warm-Up");
        tipsHeader.put(9, "Consistency");
        tipsHeader.put(10, "Rest and Recovery");

        tipsDetails.put(1, "Maintain a plank position throughout the push-up to engage your core and promote muscle balance.");
        tipsDetails.put(2, "Place your hands just wider than shoulder-width apart to ensure good form and better engage chest muscles.");
        tipsDetails.put(3, "Keep your elbows at a 45-degree angle to your body to reduce the stress on your shoulders and increase the work on your chest.");
        tipsDetails.put(4, "Use slow, controlled movements up and down to maximize muscle use and minimize momentum cheating.");
        tipsDetails.put(5, "Go down until your chest almost touches the ground and fully extend your arms on the way up for full muscle engagement.");
        tipsDetails.put(6, "Inhale on the way down and exhale on the way up. Proper breathing helps maintain rhythm and increases endurance.");
        tipsDetails.put(7, "Prioritize the quality of each push-up over the quantity. Good form ensures better strength building and reduces injury risk.");
        tipsDetails.put(8, "Always start with a good warm-up to increase blood flow to your muscles and prevent injuries.");
        tipsDetails.put(9, "Consistent practice with proper form is key to increasing your push-up count over time.");
        tipsDetails.put(10, "Allow your muscles time to rest and recover between workouts to prevent overuse injuries and improve gains.");

    }


    public String getTip() {
        Random random = new Random();
        int randomNumber = random.nextInt(10) + 1;
        return String.format(
                "{ %s: %s }",
                tipsHeader.get(randomNumber),
                tipsDetails.get(randomNumber)
        );
    }
}
