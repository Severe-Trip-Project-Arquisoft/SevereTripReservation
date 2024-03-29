package com.reservation.controller;

import com.reservation.entity.Reservation;
import com.reservation.entity.ReservationRequest;
import com.reservation.service.ReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;

@Api(value="Reservation Microservices",description="Operations related to Reservations")
@RequestMapping("/reservation/")
@RestController
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    ReservationService resService;

    @ApiOperation(value="Store a reservation",response = Reservation.class)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Reservation> saveReservation(
            @Valid @ApiParam(value = "Reservation object to store", required = true)@RequestBody ReservationRequest reservationRequest){

        try {
            return new ResponseEntity<>(resService.save(new Reservation(reservationRequest)), HttpStatus.CREATED);
        }
        catch (Exception e ){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @ApiOperation(value="Get reservation by id.",response = List.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Reservation> getById(@PathVariable Long id){
        try {
            Optional<Reservation> res = resService.getById(id);
            return res.map(reservation -> new ResponseEntity<>(reservation, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
        }
        catch (Exception e){
            return new ResponseEntity<>( HttpStatus.FORBIDDEN );
        }
    }

    @ApiOperation(value="Get all reservations",response = List.class)
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<List<Reservation>> getAllReservations(){
        try {
            return new ResponseEntity<>( resService.getAll(), HttpStatus.OK );
        }
        catch (Exception e){
            return new ResponseEntity<>( HttpStatus.FORBIDDEN );
        }
    }

    @ApiOperation(value="Get all reservations from a specific provider",response = List.class)
    @RequestMapping(value = "/provider/{providerId}", method = RequestMethod.GET)
    public ResponseEntity<List<Reservation>> getProviderReservations(
            @ApiParam(value = "Provider Id for filtering", required = true)@PathVariable("providerId") String providerId){
        try {
            return ResponseEntity.ok(resService.getAllByProviderId(providerId));
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }


    @ApiOperation(value="Get all reservations from a specific post",response = List.class)
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<List<Reservation>> getPostReservations(
            @ApiParam(value = "Post Id for filtering (String)", required = true)@PathVariable("postId") String postId){
        try{
            return ResponseEntity.ok(resService.getAllByPostId(postId));
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @ApiOperation(value="Get all reservations from a specific client",response = List.class)
    @RequestMapping(value = "/client/{clientId}", method = RequestMethod.GET)
    public ResponseEntity<List<Reservation>> getClientReservations(
            @ApiParam(value = "Client Id for filtering", required = true)@PathVariable("clientId") String clientId){

        try{
            return ResponseEntity.ok(resService.getAllByClientId(clientId));
        }

        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }


    @ApiOperation(value="Delete a reservation",response = Reservation.class)
    @RequestMapping(value="/{reservationId}",method = RequestMethod.DELETE)
    public ResponseEntity<Reservation> rejectReservation(
            @ApiParam(value = "Reservation Id", required = true)@PathVariable("reservationId") Long reservationId){
        try {

            Optional<Reservation> found = resService.getReservationById(reservationId);
            resService.rejectReservation(reservationId);
            return found.map(reservation -> new ResponseEntity<>(reservation, HttpStatus.ACCEPTED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @ApiOperation(value="Update a reservation.",response = Reservation.class)
    @RequestMapping(value = "/{reservationId}" , method = RequestMethod.PUT)
    public ResponseEntity<Reservation> updateReservation(@ApiParam(value = "Reservation body of reservation", required = true) @RequestBody ReservationRequest reservationRequest,
                                                             @ApiParam(value = "Id of reservation", required = true) @PathVariable("reservationId") Long reservationId){
        try {
            Optional<Reservation> res = resService.getById(reservationId);
            if(res.isPresent()){
                Reservation reservation = res.get();
                reservation.update(reservationRequest);
                this.resService.save(reservation);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }



    @ApiOperation(value="Answer a reservation. Not defined",response = Reservation.class)
    @RequestMapping(value = "/accept/{reservationId}" , method = RequestMethod.PUT)
    public ResponseEntity<Reservation> answerReservation(@ApiParam(value = "Id of reservation", required = true)@PathVariable("reservationId") Long reservationId){
        try {
            Reservation reservation = resService.markReservationAsAnswered(reservationId);
            if( reservation!= null) {
                return new ResponseEntity<>( reservation ,HttpStatus.ACCEPTED);
            }else{
                return new ResponseEntity<>( HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }



    @ApiOperation(value="Payment of a given reservation.",response = Reservation.class)
    @RequestMapping(value="/pay/{reservationId}",method = RequestMethod.PUT)
    public ResponseEntity<Reservation> payReservation(
            @ApiParam(value = "Reservation Id", required = true)@PathVariable("reservationId") Long reservationId){
        try{
            Optional<Reservation> found = resService.payReservation(reservationId);
            if(found.isPresent()){
                Reservation reservation = found.get();
                if(reservation.getPaid()) {
                    return new ResponseEntity<>(reservation, HttpStatus.ACCEPTED);
                }else{
                    return new ResponseEntity<>(reservation, HttpStatus.NOT_MODIFIED);
                }
            }
            else {
                return new ResponseEntity<>( HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
