package com.javahelps.jpa.test.n_plus_1_problem_reference_to_owner.native_query_solution;

import com.javahelps.jpa.test.util.PersistentHelper;
import org.hibernate.Criteria;
import org.hibernate.Session;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HibernateSpecificMapping {
    public static void main(String[] args) {
        EntityManager entityManager = PersistentHelper.getEntityManager(new Class[]{Stock.class, StockDailyRecord.class});

        saveData(entityManager);
        entityManager.clear();

        {//при использовании обычного запроса получаем n+1 проблему
            entityManager.getTransaction().begin();

            System.out.println();
            System.out.println("Before list stock native select");
            System.out.println();

            List<StockDailyRecord> results = entityManager.createNativeQuery(
                    "SELECT * FROM stock_daily_record", StockDailyRecord.class
            ).getResultList();

            System.out.println();
            System.out.println("After list stock native select");
            System.out.println();

            //снова видем n+1 проблему, не смотря на присутствие всех необходимых объектов в кэше
            for (StockDailyRecord stockDailyRecord : results) {
                System.out.println(stockDailyRecord.getId());
                System.out.println(stockDailyRecord.getStock());
            }

            entityManager.getTransaction().commit();
        }

        entityManager.clear();

        {//в случае, когда мы используем коллекцию для инициализации связи - мы не можем ничего указать в качестве алиса для поля
            //через аннотацию @FieldResult. Соответственно, хибернейт не сможет автоматически замапить эту коллекцию, даже не смотря на то
            //что данные уже загружены в память. Более того, если посмотреть на самый последний запрос, проверяющий наличие в кэше
            //первого уровня связанных сущносей - мы увидим, что сущности были распознаны хибернейтом и загружены в кэщ
            //но этого для мапинга недостаточно. Даже если мы сами попытаемся мапить два объекта и потом вставлять коллекции
            //хибернейт воспримит новые объекты как Detached сущности и будет совершать дополнительные запросы каждый раз
            entityManager.getTransaction().begin();

            System.out.println();
            System.out.println("Before list select");
            System.out.println();

//
            List<StockDailyRecord> results = ((Session)entityManager.getDelegate())
                    .createSQLQuery(
                            "SELECT {sdr.*}, {s.*} FROM stock_daily_record sdr" +
                                    " JOIN stock s ON sdr.stock_id = s.id")
                    .addEntity("sdr", StockDailyRecord.class)
                    .addJoin("s", "sdr.stock")
                    .addEntity("sdr", StockDailyRecord.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .list();

            System.out.println();
            System.out.println("After list select");
            System.out.println();

            //снова видем n+1 проблему, не смотря на присутствие всех необходимых объектов в кэше
            for (StockDailyRecord stockDailyRecord : results) {
                System.out.println(stockDailyRecord.getId());
                System.out.println(stockDailyRecord.getStock());
            }

            entityManager.getTransaction().commit();
        }

        {//используя FetchMode.SUBSELECT мы и на таком запросе приобретаем один дополнительный запрос
            entityManager.getTransaction().begin();

            System.out.println();
            System.out.println("Before one stock select");
            System.out.println();

            Stock stock = entityManager.find(Stock.class, 1L);
            StockDailyRecord stockDailyRecord = entityManager.find(StockDailyRecord.class, 1L);

            System.out.println();
            System.out.println("After one stock select");
            System.out.println();

//            System.out.println(stockDailyRecord.getStock());

            entityManager.getTransaction().commit();
        }
    }

    private static void saveData(EntityManager entityManager) {
        entityManager.getTransaction().begin();

        StockDailyRecord stockDailyRecord1 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord2 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord3 = new StockDailyRecord();

        StockDailyRecord stockDailyRecord4 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord5 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord6 = new StockDailyRecord();

        StockDailyRecord stockDailyRecord7 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord8 = new StockDailyRecord();
        StockDailyRecord stockDailyRecord9 = new StockDailyRecord();

        entityManager.persist(stockDailyRecord1);
        entityManager.persist(stockDailyRecord2);
        entityManager.persist(stockDailyRecord3);

        entityManager.persist(stockDailyRecord4);
        entityManager.persist(stockDailyRecord5);
        entityManager.persist(stockDailyRecord6);

        entityManager.persist(stockDailyRecord7);
        entityManager.persist(stockDailyRecord8);
        entityManager.persist(stockDailyRecord9);

        Stock stock1 = new Stock();
        Stock stock2 = new Stock();
        Stock stock3 = new Stock();

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);

        stockDailyRecord1.setStock(stock1);
        stockDailyRecord2.setStock(stock1);
        stockDailyRecord3.setStock(stock1);

        stockDailyRecord4.setStock(stock2);
        stockDailyRecord5.setStock(stock2);
        stockDailyRecord6.setStock(stock2);

        stockDailyRecord7.setStock(stock3);
        stockDailyRecord8.setStock(stock3);
        stockDailyRecord9.setStock(stock3);

        entityManager.getTransaction().commit();
    }


    @Entity
    @Table(name = "stock_daily_record")
    private static class StockDailyRecord {
        @Id
        @GeneratedValue
        private long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "stock_id")
        private Stock stock;

        public StockDailyRecord() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Stock getStock() {
            return stock;
        }

        public void setStock(Stock stock) {
            this.stock = stock;
        }

        @Override
        public String toString() {
            return "StockDailyRecord{" +
                    "id=" + id +
                    '}';
        }
    }

    @Entity
    @Table(name = "stock")
    private static class Stock implements Serializable {

        @Id
        @GeneratedValue
        private long id;

        public Stock() {
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Stock{" +
                    "id=" + id +
                    '}';
        }
    }
}
