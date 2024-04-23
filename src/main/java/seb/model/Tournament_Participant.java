package seb.model;


public class Tournament_Participant {
    private int tournament_id;
    private int user_id;
    private int placement;
    private int score;

    public Tournament_Participant (int tournament_id, int user_id, int placement, int score) {
        this.tournament_id = tournament_id;
        this.user_id = user_id;
        this.placement = placement;
        this.score = score;
    }

}
