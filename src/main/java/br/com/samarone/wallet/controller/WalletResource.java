package br.com.samarone.wallet.controller;

import br.com.samarone.wallet.service.WalletService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Path("/wallets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletResource {

    @Inject
    private WalletService walletService;

    @POST
    @Path("/{userId}")
    public Response createWallet(@PathParam("userId") Long userId) {
        return Response.status(Response.Status.CREATED).entity(walletService.createWallet(userId)).build();
    }

    @GET
    @Path("/{userId}/balance")
    public Response getBalance(@PathParam("userId") Long userId) {
        return walletService.getBalance(userId)
                .map(balance -> Response.ok(balance).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{userId}/balance/historical")
    public Response getHistoricalBalance(@PathParam("userId") Long userId, @QueryParam("timestamp") String timestamp) {
        LocalDateTime dateTime = LocalDateTime.parse(timestamp);
        return walletService.getHistoricalBalance(userId, dateTime)
                .map(balance -> Response.ok(balance).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/{userId}/deposit")
    public Response deposit(@PathParam("userId") Long userId, BigDecimal amount) {
        walletService.deposit(userId, amount);
        return Response.ok().build();
    }

    @POST
    @Path("/{userId}/withdraw")
    public Response withdraw(@PathParam("userId") Long userId, BigDecimal amount) {
        try {
            walletService.withdraw(userId, amount);
            return Response.ok().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/transfer")
    public Response transfer(@QueryParam("fromUserId") Long fromUserId,
                             @QueryParam("toUserId") Long toUserId,
                             BigDecimal amount) {
        try {
            walletService.transfer(fromUserId, toUserId, amount);
            return Response.ok().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
