package repository

import daos.AreaDao
import daos.PostDao
import daos.PreferenciesDao
import daos.PresentacioDao
import daos.UsuariDao

class Repository (

    val usuariDao: UsuariDao = UsuariDao(),
    val areaDao: AreaDao = AreaDao(),
    val postDao: PostDao = PostDao(),
    val presentacioDao: PresentacioDao = PresentacioDao(),
    val preferenciesDao: PreferenciesDao = PreferenciesDao()

)
