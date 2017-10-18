package neko.models.chat

sealed trait Action

object Action {

  case object Join extends Action

  case object Speak extends Action

  case object Unknown extends Action

  def from(in: String): Action = in match {
    case "Join" => Join
    case "Speak" => Speak
    case _ => Unknown
  }
}

