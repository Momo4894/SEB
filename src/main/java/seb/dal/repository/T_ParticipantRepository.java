package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Status;
import seb.model.Tournament;
import seb.model.Tournament_Participant;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class T_ParticipantRepository {
    private UnitOfWork unitOfWork;

    public T_ParticipantRepository (UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }


    public Tournament_Participant getTournamentParticipantByStatusAndUsername(String username, Status status) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select tp.* 
                        from users u 
                        join tournament_participants tp on u.id = tp.user_id 
                        join tournaments t on tp.tournament_id = t.id 
                        where u.username = ? and t.status = cast(? AS tournament_status)"""))
        {

            System.out.println("in gettournamentbystatus");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, status.getStatus());

            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                Tournament_Participant tournament_participant = new Tournament_Participant(
                        resultSet.getInt("tournament_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("placement"),
                        resultSet.getInt("score")
                );
                return tournament_participant;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void addTournamentParticipant (int user_id, int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        insert into tournament_participants (tournament_id, user_id) values (?, ?)
                        """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, tournament_id);
            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.unitOfWork.getConnection() != null) {
                this.unitOfWork.rollback(); // Rollback transaction if error occurs
            }
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }
}
