package seb.dal.repository;

import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Stats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatsRepository {
    private UnitOfWork unitOfWork;

    public StatsRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    public List<Stats> getAllStatsByUser(int user_id) {

        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from stats where user_id = ?
                        """))

        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Stats> statsRows = new ArrayList<>();
            while (resultSet.next()) {
                Stats stats = new Stats(
                        resultSet.getInt("id"),
                        resultSet.getInt("duration"),
                        resultSet.getInt("count"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("tournament_id")
                );
                statsRows.add(stats);
            }
            return statsRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }

    public int getOverAllPushupsPerUser(int user_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select count from stats where user_id = ?
                        """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            int overAllPushups = 0;
            while (resultSet.next()) {
                overAllPushups += resultSet.getInt("count");
            }
            return overAllPushups;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }

    public List<Stats> getOverAllPushupsFromAllOtherUsers(int user_id) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select count, user_id from stats where user_id != ?
                        """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Stats> stats = new ArrayList<>();
            while (resultSet.next()) {
                boolean isAdded = false;
                for (int i = 0; i < stats.size(); i++) {
                    if (stats.get(i).getUser_id() == resultSet.getInt("user_id")) {
                        stats.get(i).addToCount(resultSet.getInt("count"));
                        isAdded = true;
                    }
                }
                if (!isAdded) {
                    stats.add(new Stats(resultSet.getInt("count"), resultSet.getInt("user_id")));
                }
            }
            return stats;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }

    public void addHistory (Stats stats) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        insert into stats (type, duration, count, user_id, tournament_id) values (cast(? as exercise_type), ?, ?, ?, ?)
                        """))
        {
            preparedStatement.setString(1, stats.getName().getType());
            preparedStatement.setInt(2, stats.getDuration());
            preparedStatement.setInt(3, stats.getCount());
            preparedStatement.setInt(4, stats.getUser_id());
            preparedStatement.setInt(5, stats.getTournament_id());

            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Insert nicht erfolgreich: " + e.getMessage(), e);
        }
    }


    public int getOverAllCountByUserInTournament(int userId, int tournamentId) {
        try (PreparedStatement preparedStatement =
                this.unitOfWork.prepareStatement("""
                        select count from stats where user_id = ? and tournament_id = ?
                        """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, tournamentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            int count = 0;
            while (resultSet.next()) {
                count += resultSet.getInt("count");
            }
            return count;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich: " + e.getMessage(), e);
        }
    }
}
