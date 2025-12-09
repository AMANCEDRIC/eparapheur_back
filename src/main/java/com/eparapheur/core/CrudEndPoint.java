package com.eparapheur.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

public interface CrudEndPoint <T extends PanacheEntityBase> {

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get one by ID.",
            description = "Returns the element whose ID matches the one passed in parameter")
    Response get(@PathParam("id") Long id) throws JsonProcessingException;

    @Path("create")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create one.",
            description = "Create one entry.",
            hidden = true
    )
    @APIResponse(
            responseCode = "200"
    )
    Response create(Object entry) throws Exception;

    @Path("{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete one by ID.",
            description = "Delete the element whose ID matches the one passed in parameter")
    @APIResponse(
            responseCode = "200"
    )
    Response delete(@PathParam("id") Long id) throws Exception;


    @Path("soft/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Soft delete one by ID.",
            description = "Put element deleted property to true whose ID matches the one passed in parameter")
    @APIResponse(
            responseCode = "200"
    )
    Response softDelete(@PathParam("id") Long id) throws Exception;

    @Path("toggle/{id}/{status}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Toggle one entry status  by ID.",
            description = "Toggle element active property whose ID matches the one passed in parameter")
    @APIResponse(
            responseCode = "200"
    )
    Response toggleStatus(@PathParam("id") Long id, @PathParam("status") Boolean status);

    @Path("paginate")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get paginate list",
            description = "Returns elements list")
    @APIResponse(
            responseCode = "200"
    )
    Response paginate(@QueryParam("size") int size, @QueryParam("page") int page);

    @Path("paginate/trash")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get deleted paginate list ",
            description = "Returns deleted elements list")
    @APIResponse(
            responseCode = "200"
    )
    Response paginateTrash(@QueryParam("size") int size, @QueryParam("page") int page) throws Exception;

    /*
        @Path("paginate")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        default Response paginate(@QueryParam("size") int size, @QueryParam("page") int page) {

            ApiResponse<Pagination<T>> response = new ApiResponse<>();
            response.setStatus_code(7000);
            response.setStatus_message("Success");
            Pagination<T> pagination = new Pagination<>();
            int per_page = size > 0 ? size : 25;
            int current_page = page > 0 ? page : 1;

            System.out.println(T.count());
            // create a query for all living persons
            PanacheQuery<T> livingData = T.findAll();
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
    */
    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get entity table stats.",
            description = "Returns all stats for table")
    @APIResponse(
            responseCode = "200"
    )
    Response stats();
}
