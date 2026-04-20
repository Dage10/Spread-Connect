package repository

import daos.AreaDao
import daos.PostDao
import daos.PreferenciesDao
import daos.PresentacioDao
import daos.ReaccioDao
import daos.UsuariDao
import daos.ComentarisDao
import daos.SeguimentDao

class Repository (
    val usuariDao: UsuariDao = UsuariDao(),
    val areaDao: AreaDao = AreaDao(),
    val postDao: PostDao = PostDao(),
    val presentacioDao: PresentacioDao = PresentacioDao(),
    val preferenciesDao: PreferenciesDao = PreferenciesDao(),
    val reaccioDao: ReaccioDao = ReaccioDao(),
    val comentarisDao: ComentarisDao = ComentarisDao(),
    val seguimentDao: SeguimentDao = SeguimentDao()
)
