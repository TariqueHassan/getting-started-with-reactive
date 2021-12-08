package org.acme;

import com.google.gson.Gson;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import org.hibernate.annotations.SourceType;
import org.tinylog.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/students")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

    @GET
    @Path("/tinylog")
    @Produces(MediaType.TEXT_PLAIN)
    public void tinylogTesting() {
        Logger.info("This Info Logger!!!");
        Logger.error("This is Error Logger");
        Logger.debug("Debug Logger");
        Logger.trace("Trace Logger");
        Logger.warn("Warn Logger");

    }

    public static void method3() throws Exception {
        throw new Exception("Exception thrown in method3");
        
    }

    @GET
    @Path("/exc")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tinyLogException() {

        try {
        ArrayList<String> myList = new ArrayList<String>(); 
        myList.add("Dogs"); 
        myList.add("are"); 
        myList.add("cute."); 
        System.out.println(myList.get(3)); 
        } catch (Exception e) {

            System.out.println("Line number: "+ e.getStackTrace()[0].getLineNumber());
            System.out.println("Class Name: "+this.getClass());
            System.out.println("Method: "+e.getStackTrace()[0].getMethodName());

            return Response.ok(e.getStackTrace()).build();
            // StackTraceElement[] traceElements = e.getStackTrace();

            // for (StackTraceElement element : traceElements) {
            //     System.out.printf("%s	classname: ", element.getClassName());
            //     System.out.printf("%s	FileName: ", element.getFileName());
            //     System.out.printf("%s	LineNumber: ", element.getLineNumber());
            //     System.out.printf("%s Method: ", element.getMethodName());
            // } // end for
        }
        return Response.ok("No error").build();

    }

    BlockingNonblockingTest obj = new BlockingNonblockingTest();

    @GET
    @Path("/testing")
    public Response getBlocking() {
        obj.blockingTest();
        return Response.ok().status(201).build();
    }

    @GET
    public Uni<List<Student>> getAllStudents() {
        
        return Student.listAll(Sort.by("name"));
    }

    @GET
    @Path(("/{id}"))
    public Uni<Student> getStudentById(Long id) {
        return Student.findById(id);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Student updatedStudent) {
        if (updatedStudent == null)
            throw new WebApplicationException("Details were not updated on request", 422);
        return Panache.withTransaction(() -> Student.<Student>findById(id).onItem().ifNotNull().invoke(
                (car) -> {
                    car.setName(updatedStudent.getName());
                    car.setDepartment(updatedStudent.getDepartment());
                    car.setSemester(updatedStudent.getSemester());
                }))
                .onItem().ifNotNull().transform(car -> Response.ok(car).build())
                .onItem().ifNull()
                .continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);

    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Panache.withTransaction(() -> Student.deleteById(id))
                .map(deleted -> deleted ? Response.ok().status(Response.Status.NO_CONTENT).build()
                        : Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<Response> create(Student student) {
        return Panache.<Student>withTransaction(student::persist)
                .map(inserted -> Response.created(URI.create("/students/" + inserted.getId())).build());
    }
}
