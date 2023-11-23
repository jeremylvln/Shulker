// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: api/messages.proto

// Protobuf Java Version: 3.25.0
package com.openmatch;

public interface MatchOrBuilder extends
    // @@protoc_insertion_point(interface_extends:openmatch.Match)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * A Match ID that should be passed through the stack for tracing.
   * </pre>
   *
   * <code>string match_id = 1 [json_name = "matchId"];</code>
   * @return The matchId.
   */
  java.lang.String getMatchId();
  /**
   * <pre>
   * A Match ID that should be passed through the stack for tracing.
   * </pre>
   *
   * <code>string match_id = 1 [json_name = "matchId"];</code>
   * @return The bytes for matchId.
   */
  com.google.protobuf.ByteString
      getMatchIdBytes();

  /**
   * <pre>
   * Name of the match profile that generated this Match.
   * </pre>
   *
   * <code>string match_profile = 2 [json_name = "matchProfile"];</code>
   * @return The matchProfile.
   */
  java.lang.String getMatchProfile();
  /**
   * <pre>
   * Name of the match profile that generated this Match.
   * </pre>
   *
   * <code>string match_profile = 2 [json_name = "matchProfile"];</code>
   * @return The bytes for matchProfile.
   */
  com.google.protobuf.ByteString
      getMatchProfileBytes();

  /**
   * <pre>
   * Name of the match function that generated this Match.
   * </pre>
   *
   * <code>string match_function = 3 [json_name = "matchFunction"];</code>
   * @return The matchFunction.
   */
  java.lang.String getMatchFunction();
  /**
   * <pre>
   * Name of the match function that generated this Match.
   * </pre>
   *
   * <code>string match_function = 3 [json_name = "matchFunction"];</code>
   * @return The bytes for matchFunction.
   */
  com.google.protobuf.ByteString
      getMatchFunctionBytes();

  /**
   * <pre>
   * Tickets belonging to this match.
   * </pre>
   *
   * <code>repeated .openmatch.Ticket tickets = 4 [json_name = "tickets"];</code>
   */
  java.util.List<com.openmatch.Ticket> 
      getTicketsList();
  /**
   * <pre>
   * Tickets belonging to this match.
   * </pre>
   *
   * <code>repeated .openmatch.Ticket tickets = 4 [json_name = "tickets"];</code>
   */
  com.openmatch.Ticket getTickets(int index);
  /**
   * <pre>
   * Tickets belonging to this match.
   * </pre>
   *
   * <code>repeated .openmatch.Ticket tickets = 4 [json_name = "tickets"];</code>
   */
  int getTicketsCount();
  /**
   * <pre>
   * Tickets belonging to this match.
   * </pre>
   *
   * <code>repeated .openmatch.Ticket tickets = 4 [json_name = "tickets"];</code>
   */
  java.util.List<? extends com.openmatch.TicketOrBuilder> 
      getTicketsOrBuilderList();
  /**
   * <pre>
   * Tickets belonging to this match.
   * </pre>
   *
   * <code>repeated .openmatch.Ticket tickets = 4 [json_name = "tickets"];</code>
   */
  com.openmatch.TicketOrBuilder getTicketsOrBuilder(
      int index);

  /**
   * <pre>
   * Customized information not inspected by Open Match, to be used by the match
   * making function, evaluator, and components making calls to Open Match.
   * Optional, depending on the requirements of the connected systems.
   * </pre>
   *
   * <code>map&lt;string, .google.protobuf.Any&gt; extensions = 7 [json_name = "extensions"];</code>
   */
  int getExtensionsCount();
  /**
   * <pre>
   * Customized information not inspected by Open Match, to be used by the match
   * making function, evaluator, and components making calls to Open Match.
   * Optional, depending on the requirements of the connected systems.
   * </pre>
   *
   * <code>map&lt;string, .google.protobuf.Any&gt; extensions = 7 [json_name = "extensions"];</code>
   */
  boolean containsExtensions(
      java.lang.String key);
  /**
   * Use {@link #getExtensionsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.google.protobuf.Any>
  getExtensions();
  /**
   * <pre>
   * Customized information not inspected by Open Match, to be used by the match
   * making function, evaluator, and components making calls to Open Match.
   * Optional, depending on the requirements of the connected systems.
   * </pre>
   *
   * <code>map&lt;string, .google.protobuf.Any&gt; extensions = 7 [json_name = "extensions"];</code>
   */
  java.util.Map<java.lang.String, com.google.protobuf.Any>
  getExtensionsMap();
  /**
   * <pre>
   * Customized information not inspected by Open Match, to be used by the match
   * making function, evaluator, and components making calls to Open Match.
   * Optional, depending on the requirements of the connected systems.
   * </pre>
   *
   * <code>map&lt;string, .google.protobuf.Any&gt; extensions = 7 [json_name = "extensions"];</code>
   */
  /* nullable */
com.google.protobuf.Any getExtensionsOrDefault(
      java.lang.String key,
      /* nullable */
com.google.protobuf.Any defaultValue);
  /**
   * <pre>
   * Customized information not inspected by Open Match, to be used by the match
   * making function, evaluator, and components making calls to Open Match.
   * Optional, depending on the requirements of the connected systems.
   * </pre>
   *
   * <code>map&lt;string, .google.protobuf.Any&gt; extensions = 7 [json_name = "extensions"];</code>
   */
  com.google.protobuf.Any getExtensionsOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * Backfill request which contains additional information to the match
   * and contains an association to a GameServer.
   * BETA FEATURE WARNING: This field is not finalized and still subject
   * to possible change or removal.
   * </pre>
   *
   * <code>.openmatch.Backfill backfill = 8 [json_name = "backfill"];</code>
   * @return Whether the backfill field is set.
   */
  boolean hasBackfill();
  /**
   * <pre>
   * Backfill request which contains additional information to the match
   * and contains an association to a GameServer.
   * BETA FEATURE WARNING: This field is not finalized and still subject
   * to possible change or removal.
   * </pre>
   *
   * <code>.openmatch.Backfill backfill = 8 [json_name = "backfill"];</code>
   * @return The backfill.
   */
  com.openmatch.Backfill getBackfill();
  /**
   * <pre>
   * Backfill request which contains additional information to the match
   * and contains an association to a GameServer.
   * BETA FEATURE WARNING: This field is not finalized and still subject
   * to possible change or removal.
   * </pre>
   *
   * <code>.openmatch.Backfill backfill = 8 [json_name = "backfill"];</code>
   */
  com.openmatch.BackfillOrBuilder getBackfillOrBuilder();

  /**
   * <pre>
   * AllocateGameServer signalise Director that Backfill is new and it should 
   * allocate a GameServer, this Backfill would be assigned.
   * BETA FEATURE WARNING: This field is not finalized and still subject
   * to possible change or removal.
   * </pre>
   *
   * <code>bool allocate_gameserver = 9 [json_name = "allocateGameserver"];</code>
   * @return The allocateGameserver.
   */
  boolean getAllocateGameserver();
}