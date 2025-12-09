package com.eparapheur.core;

import com.eparapheur.core.features.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jose4j.json.internal.json_simple.JSONObject;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public  abstract class  CrudEndPointImpl <T extends PanacheEntityBase> implements CrudEndPoint<T> {

    private PanacheRepository<T> t;
    public PanacheRepository<T> getT() {
        return t;
    }
    public void setT(PanacheRepository<T> t) {
        this.t = t;
        System.out.println(t.toString());
    }

    @Override
    public Response create(Object entry) throws Exception {
        throw new Exception("Not yet");
    }

    @Override
    public Response get(Long id) throws JsonProcessingException {
        ApiResponse<T> response = new ApiResponse<>();
        T livingData = t.find("id", id).firstResult();
        if (livingData == null) {
            response.setStatus_code(8004);
            response.setStatus_message("Not found");
        } else {
            response.setStatus_code(7000);
            response.setStatus_message("Success");
        }
        response.setData(livingData);
        return Response.status(200).entity(response).build();
    }

    @Override
    public Response delete(Long id) throws Exception {
        ApiResponse<T> response = new ApiResponse<>();
        T livingData = t.findById(id);
        if (livingData == null) {
            response.setStatus_code(404);
            response.setStatus_message("Not found");
        } else {
            if (t.deleteById(id)) {
                response.setStatus_code(200);
                response.setStatus_message("Deleted successfully");
            } else {
                response.setStatus_code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                response.setStatus_message("Deletion failed");
            }
        }
        response.setData(livingData);
        return Response.status(200).entity(response).build();
    }

    @Override
    @Transactional
    public Response softDelete(Long id) throws Exception {
        ApiResponse<T> response = new ApiResponse<>();
        try {

            T livingData = t.findById(id);

            if (livingData == null) {
                response.setStatus_code(404);
                response.setStatus_message("Not found");
            } else {
                if (t.update("active = 0 , deleted = 1 ,deleted_at = CURRENT_TIMESTAMP where id = :id ",
                        Map.of("id", id)) > 0) {
                    response.setStatus_code(200);
                    response.setStatus_message("Success");
                } else {
                    response.setStatus_code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());                    response.setStatus_message("Fail to delete");
                }
            }
            response.setData(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus_code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.setStatus_message(ex.getLocalizedMessage());
        }
        return Response.status(200).entity(response).build();
    }

    @Override
    @Transactional
    public Response toggleStatus(Long id, Boolean status) {
        ApiResponse<T> response = new ApiResponse<>();
        try {

            T livingData = t.find("id", id).firstResult();

            if (livingData == null) {
                response.setStatus_code(404);
                response.setStatus_message("Not found");
            } else {
                //, updated_at = CURRENT_TIMESTAMP
                if (t.update("active = :active ,updatedAt = :updatedAt where id = :id ",
                        Map.of("active", status,
                                "updatedAt", Instant.now(),
                                "id", id)) > 0) {
                    response.setStatus_code(200);
                    response.setStatus_message("Success");
                } else {
                    response.setStatus_code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    response.setStatus_message("Fail to delete");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        response.setData(null);
        return Response.status(200).entity(response).build();
    }

    @Override
    public Response paginate(int size, int page) {
        System.out.println("-----------------------------");
        System.out.println(t.toString());
        System.out.println("-----------------------------");
        ApiResponse<Pagination<T>> response = new ApiResponse<>();
        try {

            response.setStatus_code(7000);
            response.setStatus_message("Success");
            Pagination<T> pagination = new Pagination<>();
            int per_page = size > 0 ? size : 25;
            int current_page = page > 0 ? page : 1;

            System.out.println(t.count());
            // create a query for all living persons
            PanacheQuery<T> livingData = t.find("(deleted=:deleted OR deleted is NULL)", Map.of("deleted", false));
            Long total = livingData.count();
            // make it use pages of 25 entries at a time
            livingData.page(Page.ofSize(per_page)).pageCount();

            int total_page = livingData.page(Page.ofSize(per_page)).pageCount();
            // get the first page
            List<T> firstPage = livingData.page(current_page - 1, per_page).list();

            //construct pagination model data
            pagination.setTotal(total);
            pagination.setTotal_of_pages(total_page);
            pagination.setItems(firstPage);
            pagination.setCurrent_page(current_page);
            pagination.setNext_page(current_page + 1 <= total_page ? current_page + 1 : current_page);
            response.setData(pagination);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus_code(Response.Status.BAD_REQUEST.getStatusCode());
            response.setStatus_message(ex.getLocalizedMessage());
        }

        return Response.status(200).entity(response).build();
    }

    @Override
    public Response paginateTrash(int size, int page) throws Exception {
        System.out.println("-----------------------------");
        System.out.println(t.toString());
        System.out.println("-----------------------------");
        ApiResponse<Pagination<T>> response = new ApiResponse<>();
        response.setStatus_code(200);
        response.setStatus_message("Success");
        Pagination<T> pagination = new Pagination<>();
        int per_page = size > 0 ? size : 25;
        int current_page = page > 0 ? page : 1;

        System.out.println(t.count());
        // create a query for all living persons
        PanacheQuery<T> livingData = t.find("deleted=:deleted", Map.of("deleted", true));
        Long total = livingData.count();
        // make it use pages of 25 entries at a time
        livingData.page(Page.ofSize(per_page)).pageCount();

        int total_page = livingData.page(Page.ofSize(per_page)).pageCount();
        // get the first page
        List<T> firstPage = livingData.page(current_page - 1, per_page).list();

        //construct pagination model data
        pagination.setTotal(total);
        pagination.setTotal_of_pages(total_page);
        pagination.setItems(firstPage);
        pagination.setCurrent_page(current_page);
        pagination.setNext_page(current_page + 1 <= total_page ? current_page + 1 : current_page);


        response.setData(pagination);
        return Response.status(200).entity(response).build();
    }

    @Override
    public Response stats() {
        JSONObject vs = new JSONObject();
        vs.put("total", t.count());
        //vs.put("Entity", );
        return Response.ok(vs.toJSONString()).build();
    }


}
