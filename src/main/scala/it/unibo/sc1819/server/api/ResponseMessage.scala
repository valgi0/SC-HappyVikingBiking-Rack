

package it.unibo.sc1819.server.api

/**
  * This class is used for define the message accepted to the RouterResponse.
  *
  */

sealed trait JsonResponse

case class Message(message: String) extends JsonResponse
case class Error(cause: Option[String] = None) extends JsonResponse

case class GameFound(gameId: String) extends JsonResponse
case class ServerContextEncoder(ipAddress: String, port: Int) extends JsonResponse
case class GameHistory(gameId: String, teams: Seq[Side], gameSet: GameSet) extends JsonResponse
case class Side(members: Seq[String]) extends JsonResponse
case class User(username: String, score: Int) extends JsonResponse
case class UserFriends(username: String, friends: Seq[String]) extends JsonResponse


case class SavedMatches(games: Seq[StoredMatch]) extends JsonResponse
case class StoredMatch(gameId: String, team1: Side, team2: Side)

case class Matches(games: Seq[LiveGame]) extends JsonResponse
case class LiveGame(gameId: String, team1: Side, team2: Side, gameType: String) extends JsonResponse

case class Ranking(members: Seq[RankElement]) extends JsonResponse
case class RankElement(player: String, score: Long)


/**
  * This class represents a Full game.
  *
  * @param players
  * Players in the match.
  * @param turns
  * All game's turn.
  * @param winners
  * username of the game winners.
  */
case class Game(players: Seq[String], turns: Seq[GameSet], winners: Seq[String]) extends JsonResponse

/**
  * A game set.
  *
  * @param playersHand
  * Map of player and respective set cards.
  * @param hands
  * All the 10 played hand in this set
  * @param briscola
  * GameSet' s briscola
  * @param team1Score
  * Score at the end of this set for the team1.
  * The score is global, so, can be 11 - 0 / 22 - 0 / 33 - 0 / ecc..
  * @param team2Score
  * Score at the end of this set for the team2.
  * The score is global, so, can be 11 - 0 / 22 - 0 / 33 - 0 / ecc..
  */
case class GameSet(playersHand: Map[String, Set[String]], hands: Seq[Hand], briscola: String, team1Score: Int, team2Score: Int) extends JsonResponse

/**
  * Sequence of 4 moves and the name of the final taker
  * @param moves
  *              Player and respective card played.
  * @param taker
  *              Hand's taker.
  */
case class Hand(moves: Seq[Move], taker: String) extends JsonResponse

/**
  * Simulate a card player.
  * @param player
  *               Username of the player who played the card.
  * @param card
  *             the card serialized card. Ex -> Coin4 / Club10 / ecc..
  */
case class Move(player: String, card: String) extends JsonResponse

