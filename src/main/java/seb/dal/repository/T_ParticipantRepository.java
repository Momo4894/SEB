package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Status;
import seb.model.Tournament_Participant;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void addTournamentParticipant (int user_id, int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        insert into tournament_participants (tournament_id, user_id) values (?, ?)
                        """))
        {
            preparedStatement.setInt(1, tournament_id);
            preparedStatement.setInt(2, user_id);
            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("insert nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public Tournament_Participant getTournamentParticipantByIds(int userId, int tournamentId) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select * from tournament_participants where user_id = ? and tournament_id = ?
                        """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, tournamentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Tournament_Participant(
                        resultSet.getInt("tournament_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("placement"),
                        resultSet.getInt("score")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    private void setPlacements(int user_id, int placement, int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        update tournament_participants set placement = ? where user_id = ? and tournament_id = ?"""
                ))
        {
            preparedStatement.setInt(1, placement);
            preparedStatement.setInt(2, user_id);
            preparedStatement.setInt(3, tournament_id);

            preparedStatement.executeUpdate();
            this.unitOfWork.commitTransaction();

        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void updatePlacements(int tournament_id) {
        try (PreparedStatement preparedStatement =
                     unitOfWork.prepareStatement("""
                             select user_id, score from tournament_participants where tournament_id = ? order by score desc
                             """))
        {
            preparedStatement.setInt(1, tournament_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            int currentScore = 0;
            int currentPlacement = 0;
            while (resultSet.next()) {
                if(currentScore != resultSet.getInt("score")) {
                    currentScore = resultSet.getInt("score");
                    ++currentPlacement;
                }
                setPlacements(resultSet.getInt("user_id"), currentPlacement, tournament_id);

            }
        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public void addScoreById(int tournamentId, int userId, int totalCountByUser) {
        try (PreparedStatement preparedStatement =
                     unitOfWork.prepareStatement("""
                             update tournament_participants set score = ? where tournament_id = ? and user_id = ?
                             """))
        {
            preparedStatement.setInt(1, totalCountByUser);
            preparedStatement.setInt(2, tournamentId);
            preparedStatement.setInt(3, userId);

            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("insert nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public List<String> getFirstPlaceUsernameByTournamentId(int tournament_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select u.username 
                        from tournament_participants tp 
                        join users u on tp.user_id = u.id 
                        where tp.tournament_id = ? and tp.placement = ?
                        """))
        {
            preparedStatement.setInt(1, tournament_id);
            preparedStatement.setInt(2, 1);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> placementList = new ArrayList<>();

            while (resultSet.next()) {
                placementList.add(resultSet.getString("username"));
            }
            return placementList;
        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public int getParticipantAmountByTournamentId(int tournament_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             select count(user_id) from tournament_participants where tournament_id = ?
                             """))
        {
            preparedStatement.setInt(1, tournament_id);
            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
            return 0;

        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }

    public Map<Integer, Integer> getPlacementsByTournamentId(int tournamentId) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select user_id, placement from tournament_participants where tournament_id = ? order by score desc
                        """))
        {
            preparedStatement.setInt(1, tournamentId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, Integer> userIdPlacement = new HashMap<>();
            while (resultSet.next()) {
                userIdPlacement.put(resultSet.getInt("user_id"), resultSet.getInt("placement"));
            }
            return userIdPlacement;
        } catch (SQLException e) {
            throw new DataAccessException("select nicht erfolgreich" + e.getMessage(), e);
        }
    }
}
