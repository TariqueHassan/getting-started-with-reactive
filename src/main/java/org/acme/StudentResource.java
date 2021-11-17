package org.acme;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/students")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

    @GET
    public Uni<List<Student>> getAllStudents() {
        return Student.listAll(Sort.by("name"));
    }

    @GET
    @Path(("/{id}"))
    public Uni<Student> getStudentById(Long id){
        return Student.findById(id);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Student updatedStudent) {
        if (updatedStudent == null)
            throw new WebApplicationException("Details were not updated on request", 422);
        return Panache.withTransaction( () -> Student.<Student>findById(id).
                        onItem().
                        ifNotNull().
                        invoke(
                                (car) -> {
                                    car.setName(updatedStudent.getName());
                                    car.setDepartment(updatedStudent.getDepartment());
                                    car.setSemester(updatedStudent.getSemester());
                                }
                        )
                )
                .onItem().ifNotNull().
                transform(car -> Response.ok(car).build())
                .onItem().ifNull()
                .continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);

    }
    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Panache.withTransaction( () -> Student.deleteById(id) )
                .map( deleted -> deleted ? Response.ok().status(Response.Status.NO_CONTENT).build() :
                        Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<Response> create(Student student) {
        return Panache.<Student>withTransaction(student::persist)
                .map(inserted -> Response.created(URI.create("/students/" + inserted.getId())).build());
    }
}
