module Types exposing (..)

import Json.Encode as Enc
import Json.Encode exposing (Value, object)
import Json.Decode as Decode exposing (Decoder, int, string, nullable)
import Json.Decode.Pipeline exposing (decode, required, optional, hardcoded)
import Maybe exposing (withDefault)


type Action
    = Join
    | Speak
    | Unknown


actionDecoder : Decoder Action
actionDecoder =
    Decode.map
        (\str ->
            case str of
                "Join" ->
                    Join

                "Speak" ->
                    Speak

                _ ->
                    Unknown
        )
        string


encodeAction : Action -> Value
encodeAction action =
    case action of
        Join ->
            Enc.string "Join"

        Speak ->
            Enc.string "Speak"

        _ ->
            Enc.string "Unknown"


type alias Message =
    { action : Action
    , author : String
    , payload : String
    , timestamp : Maybe Int
    }


messageDecoder : Decoder Message
messageDecoder =
    decode Message
        |> required "action" actionDecoder
        |> required "author" string
        |> required "payload" string
        |> required "timestamp" (nullable int)


encodeMessage : Message -> Value
encodeMessage msg =
    object
        [ ( "action", encodeAction msg.action )
        , ( "author", Enc.string msg.author )
        , ( "payload", Enc.string msg.payload )
        , ( "timestamp", Enc.int (withDefault 0 msg.timestamp) )
        ]
