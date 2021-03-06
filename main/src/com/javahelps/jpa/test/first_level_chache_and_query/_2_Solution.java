package com.javahelps.jpa.test.first_level_chache_and_query;

import com.javahelps.jpa.test.model.Post;
import com.javahelps.jpa.test.model.PostComment;
import com.javahelps.jpa.test.util.PersistentHelper;

import javax.persistence.EntityManager;

public class _2_Solution {
    public static void main(String[] args) {
        EntityManager entityManager = PersistentHelper.getEntityManager(new Class[] {Post.class, PostComment.class});

        saveObjects(entityManager);
        changeReviewByObjectState(entityManager);
        changeReviewByQuery(entityManager);
        changeReviewByNativeQuery(entityManager);


        entityManager.getTransaction().begin();

        PostComment postComment = entityManager.find(PostComment.class, 1L);

        entityManager.getTransaction().commit();

        //обновляем данные в кэше первого уровня
        System.out.println("Проверка на содержание");
        if (entityManager.contains(postComment)) {
            entityManager.refresh(postComment);
        }

        System.out.println(postComment);
    }

    private static void changeReviewByNativeQuery(EntityManager entityManager) {
        entityManager.getTransaction().begin();

        entityManager.createNativeQuery("update post_comment set review='Comment 4' where id=1").executeUpdate();

        entityManager.getTransaction().commit();
    }

    private static void changeReviewByQuery(EntityManager entityManager) {
        entityManager.getTransaction().begin();

        entityManager.createQuery("UPDATE PostComment pc SET pc.review = 'Comment 3' WHERE pc.id = 1").executeUpdate();

        entityManager.getTransaction().commit();
    }

    private static void changeReviewByObjectState(EntityManager entityManager) {
        entityManager.getTransaction().begin();

        PostComment postComment = entityManager.find(PostComment.class, 1L);

        postComment.setReview("Comment 2");

        entityManager.getTransaction().commit();
    }

    private static void saveObjects(EntityManager entityManager) {
        Post post = new Post("Post 1");
        PostComment postComment = new PostComment("Comment 1");

        entityManager.getTransaction().begin();

        //сохраняем объекты
        entityManager.persist(post);
        post.addComment(postComment);

        entityManager.getTransaction().commit();
    }
}
