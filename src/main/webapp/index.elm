module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Json.Encode exposing (encode)
import Json.Decode exposing (decodeString)
import WebSocket
import Types exposing (..)
import Debug exposing (log)
import Date exposing (fromTime)
import Date.Format exposing (format)


main =
    Html.programWithFlags
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


init : Flags -> ( Model, Cmd Msg )
init { host } =
    ( Model ("ws://" ++ host ++ "/chat") "" False "" [], Cmd.none )



-- MODEL


type alias Flags =
    { host : String }


type alias Model =
    { serverUrl : String
    , input : String
    , joined : Bool
    , username : String
    , messages : List Message
    }



-- UPDATE


type Msg
    = Input String
    | JoinSession
    | SendMessage
    | ReceiveMessage String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Input newInput ->
            ( { model | input = newInput }, Cmd.none )

        JoinSession ->
            let
                username =
                    model.input
            in
                ( { model | username = model.input, input = "" }, WebSocket.send model.serverUrl (messageToJson (Message Join username "" Nothing)) )

        SendMessage ->
            ( { model | input = "" }, WebSocket.send model.serverUrl (messageToJson (Message Speak model.username model.input Nothing)) )

        ReceiveMessage str ->
            case decodeString messageDecoder str of
                Err msg ->
                    log msg ( model, Cmd.none )

                Ok msg ->
                    case msg.action of
                        Join ->
                            let
                                joined =
                                    if model.joined then
                                        True
                                    else
                                        model.username == msg.author
                            in
                                ( { model | joined = joined, messages = (msg :: model.messages) }, Cmd.none )

                        Speak ->
                            ( { model | messages = (msg :: model.messages) }, Cmd.none )

                        otherwise ->
                            ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen model.serverUrl ReceiveMessage



-- VIEW


view : Model -> Html Msg
view model =
    if model.joined then
        div []
            [ span [] [ text "Enter the message" ]
            , input [ onInput Input, value model.input ] []
            , button [ disabled (String.isEmpty model.input), onClick SendMessage ] [ text "Send" ]
            , div [] (List.map viewMessage (List.reverse model.messages))
            ]
    else
        div []
            [ span [] [ text "Please enter your name" ]
            , input [ onInput Input, value model.input ] []
            , button [ disabled (String.isEmpty model.input), onClick JoinSession ] [ text "Join" ]
            ]



-- HELPERS


viewMessage : Message -> Html msg
viewMessage msg =
    div [] [ text (displayMessage msg) ]


messageToJson : Message -> String
messageToJson msg =
    encode 0 (encodeMessage msg)


displayMessage : Message -> String
displayMessage msg =
    "["
        ++ (formatTimestamp (Maybe.withDefault 0 msg.timestamp))
        ++ "] "
        ++ case msg.action of
            Join ->
                msg.author ++ " has joined"

            Speak ->
                msg.author ++ " : " ++ msg.payload

            _ ->
                ""


formatTimestamp : Int -> String
formatTimestamp ts =
    (toFloat ts) |> fromTime |> format ("%H:%M:%S")
