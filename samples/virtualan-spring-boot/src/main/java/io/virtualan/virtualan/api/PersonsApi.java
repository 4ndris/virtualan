/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (5.4.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package io.virtualan.virtualan.api;

import io.virtualan.virtualan.to.Error;
import io.virtualan.virtualan.to.PagedPersons;
import io.virtualan.virtualan.to.Person;
import io.virtualan.virtualan.to.Persons;
import io.virtualan.virtualan.to.PagedCollectingItems;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtualan.annotation.ApiVirtual;
import io.virtualan.annotation.VirtualService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Optional;
import javax.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-04T23:51:58.264811300-06:00[America/Chicago]")
@Validated
@Tag(name = "persons", description = "the persons API")
@VirtualService
public interface PersonsApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /persons : Gets some persons
     * Returns a list containing all persons. The list supports paging.
     *
     * @param pageSize Number of persons returned (optional)
     * @param pageNumber Page number (optional)
     * @return A list of Person (status code 200)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsGet",
        summary = "Gets some persons",
        responses = {
            @ApiResponse(responseCode = "200", description = "A list of Person", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Persons.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/persons",
        produces = { "application/json" }
    )
    default ResponseEntity<Persons> personsGet(
        @Min(0) @Max(100) @Parameter(name = "pageSize", description = "Number of persons returned", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(name = "pageNumber", description = "Page number", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"items\" : [ { \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" }, { \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" }, { \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" }, { \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" }, { \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * POST /persons : Creates a person
     * Adds a new person to the persons list.
     *
     * @param person The person to create. (optional)
     * @return Person succesfully created. (status code 204)
     *         or Person couldn&#39;t have been created. (status code 400)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsPost",
        summary = "Creates a person",
        responses = {
            @ApiResponse(responseCode = "204", description = "Person succesfully created."),
            @ApiResponse(responseCode = "400", description = "Person couldn't have been created."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/persons",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    default ResponseEntity<Void> personsPost(
        @Parameter(name = "person", description = "The person to create.", schema = @Schema(description = "")) @Valid @RequestBody(required = false) Person person
    ) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /persons/{username}/collectingItems : Gets a person&#39;s collecting items list
     * Returns a list containing all items this person is looking for. The list supports paging.
     *
     * @param username The person&#39;s username (required)
     * @param pageSize Number of persons returned (optional)
     * @param pageNumber Page number (optional)
     * @return A collected items list (status code 200)
     *         or Person does not exist. (status code 404)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsUsernameCollectingItemsGet",
        summary = "Gets a person's collecting items list",
        responses = {
            @ApiResponse(responseCode = "200", description = "A collected items list", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  PagedCollectingItems.class))),
            @ApiResponse(responseCode = "404", description = "Person does not exist."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/persons/{username}/collectingItems",
        produces = { "application/json" }
    )
    default ResponseEntity<PagedCollectingItems> personsUsernameCollectingItemsGet(
        @Parameter(name = "username", description = "The person's username", required = true, schema = @Schema(description = "")) @PathVariable("username") String username,
        @Min(0) @Max(100) @Parameter(name = "pageSize", description = "Number of persons returned", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(name = "pageNumber", description = "Page number", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "null";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * DELETE /persons/{username} : Deletes a person
     * Delete a single person identified via its username
     *
     * @param username The person&#39;s username (required)
     * @return Person successfully deleted. (status code 204)
     *         or Person does not exist. (status code 404)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsUsernameDelete",
        summary = "Deletes a person",
        responses = {
            @ApiResponse(responseCode = "204", description = "Person successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Person does not exist."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/persons/{username}",
        produces = { "application/json" }
    )
    default ResponseEntity<Void> personsUsernameDelete(
        @Parameter(name = "username", description = "The person's username", required = true, schema = @Schema(description = "")) @PathVariable("username") String username
    ) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /persons/{username}/friends : Gets a person&#39;s friends
     * Returns a list containing all persons. The list supports paging.
     *
     * @param username The person&#39;s username (required)
     * @param pageSize Number of persons returned (optional)
     * @param pageNumber Page number (optional)
     * @return A person&#39;s friends list (status code 200)
     *         or Person does not exist. (status code 404)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsUsernameFriendsGet",
        summary = "Gets a person's friends",
        responses = {
            @ApiResponse(responseCode = "200", description = "A person's friends list", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  PagedPersons.class))),
            @ApiResponse(responseCode = "404", description = "Person does not exist."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/persons/{username}/friends",
        produces = { "application/json" }
    )
    default ResponseEntity<PagedPersons> personsUsernameFriendsGet(
        @Parameter(name = "username", description = "The person's username", required = true, schema = @Schema(description = "")) @PathVariable("username") String username,
        @Min(0) @Max(100) @Parameter(name = "pageSize", description = "Number of persons returned", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(name = "pageNumber", description = "Page number", schema = @Schema(description = "")) @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "null";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /persons/{username} : Gets a person
     * Returns a single person for its username.
     *
     * @param username The person&#39;s username (required)
     * @return A Person (status code 200)
     *         or Person does not exist. (status code 404)
     *         or An unexpected error occured. (status code 500)
     */
    @ApiVirtual
    @Operation(
        operationId = "personsUsernameGet",
        summary = "Gets a person",
        responses = {
            @ApiResponse(responseCode = "200", description = "A Person", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Person.class))),
            @ApiResponse(responseCode = "404", description = "Person does not exist."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occured.", content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Error.class)))
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/persons/{username}",
        produces = { "application/json" }
    )
    default ResponseEntity<Person> personsUsernameGet(
        @Parameter(name = "username", description = "The person's username", required = true, schema = @Schema(description = "")) @PathVariable("username") String username
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"firstName\" : \"firstName\", \"lastName\" : \"lastName\", \"lastTimeOnline\" : \"2000-01-23T04:56:07.000+00:00\", \"dateOfBirth\" : \"2000-01-23\", \"spokenLanguages\" : { \"key\" : \"spokenLanguages\" }, \"username\" : \"username\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
