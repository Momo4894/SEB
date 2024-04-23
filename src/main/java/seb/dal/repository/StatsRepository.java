package seb.dal.repository;

import net.bytebuddy.dynamic.scaffold.MethodRegistry;
import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.model.Stats;
import seb.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class StatsRepository {
    private UnitOfWork unitOfWork;

    public StatsRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    public Collection<Stats> getAllStatsByUser(int user_id) {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        select * from stats where user_id = ?
                        """))

        {
            System.out.println("before setInt");
            preparedStatement.setInt(1, user_id);
            System.out.println("after setInt");
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("after execute query");
            Collection<Stats> statsRows = new ArrayList<>();
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
                        insert into stats (type, duration, count, user_id, tournament_id) values (?, ?, ?, ?, ?)
                        """))
        {
            preparedStatement.setString(1, stats.getName().getType());
            preparedStatement.setInt(2, stats.getDuration());
            preparedStatement.setInt(3, stats.getCount());
            preparedStatement.setInt(4, stats.getUser_id());
            preparedStatement.setInt(3, stats.getTournament_id());

            preparedStatement.executeUpdate();
            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Insert nicht erfolgreich: " + e.getMessage(), e);
        }
    }


}
